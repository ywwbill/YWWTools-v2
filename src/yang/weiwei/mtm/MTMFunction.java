package yang.weiwei.mtm;

import cc.mallet.optimize.Optimizable.ByGradientValue;
import org.ejml.simple.SimpleMatrix;

public class MTMFunction implements ByGradientValue
{
	private int numTopicsS,numTopicsT,n,reg;
	private double[][] omegaS,omegaT,rhoA,rhoB;
	private double[] weights;
	private double lambda;
	
	public MTMFunction(double[][] omegaS, double[][] omegaT, double[] weights, double[][] rhoA, double[][] rhoB, MTMParam param)
	{
		numTopicsS=rhoA.length;
		numTopicsT=rhoA[0].length;
		n=weights.length;
		this.omegaS=omegaS.clone();
		this.omegaT=omegaT.clone();
		this.weights=weights.clone();
		this.rhoA=rhoA.clone();
		this.rhoB=rhoB.clone();
		this.reg=param.reg;
		this.lambda=param.lambda;
	}
	
	public double getValue()
	{
		SimpleMatrix rhoAMatrix=new SimpleMatrix(rhoA);
		SimpleMatrix omegaTMatrix=new SimpleMatrix(omegaT).transpose();
		SimpleMatrix tOmega=rhoAMatrix.mult(omegaTMatrix).transpose();
		
		double value=0.0;
		for (int i=0; i<n; i++)
		{
			double dis=0.0;
			for (int t1=0; t1<numTopicsS; t1++)
			{
				double temp=omegaS[i][t1]-tOmega.get(i, t1);
				dis+=temp*temp;
			}
			value+=0.5*weights[i]*Math.log(dis);
		}
		if (reg==MTMParam.IDENTICAL)
		{
			SimpleMatrix rhoBMatrix=new SimpleMatrix(rhoB);
			SimpleMatrix rhoCMatrix=rhoAMatrix.mult(rhoBMatrix);
			for (int i=0; i<numTopicsS; i++)
			{
				for (int j=0; j<numTopicsS; j++)
				{
					if (i==j)
					{
						value+=lambda*n*(rhoCMatrix.get(i, i)-1.0)*(rhoCMatrix.get(i, i)-1.0);
					}
					else
					{
						value+=lambda*n*rhoCMatrix.get(i, j)*rhoCMatrix.get(i, j);
					}
				}
			}
		}
		return -value;
	}
	
	public void getValueGradient(double gradient[])
	{
		SimpleMatrix rhoAMatrix=new SimpleMatrix(rhoA);
		SimpleMatrix omegaTMatrix=new SimpleMatrix(omegaT).transpose();
		SimpleMatrix tOmega=rhoAMatrix.mult(omegaTMatrix).transpose();
		
		double[][] rhoGrad=new double[numTopicsS][numTopicsT];
		for (int i=0; i<n; i++)
		{
			double dis=0.0;
			for (int t1=0; t1<numTopicsS; t1++)
			{
				double temp=omegaS[i][t1]-tOmega.get(i, t1);
				dis+=temp*temp;
			}
			for (int t1=0; t1<numTopicsS; t1++)
			{
				for (int t2=0; t2<numTopicsT; t2++)
				{
					rhoGrad[t1][t2]-=weights[i]*omegaT[i][t2]*(omegaS[i][t1]-tOmega.get(i, t1))/dis;
				}
			}
		}
		if (reg==MTMParam.IDENTICAL)
		{
			SimpleMatrix rhoBMatrix=new SimpleMatrix(rhoB);
			SimpleMatrix rhoCMatrix=rhoAMatrix.mult(rhoBMatrix);
			for (int t1=0; t1<numTopicsS; t1++)
			{
				for (int t2=0; t2<numTopicsT; t2++)
				{
					rhoGrad[t1][t2]+=2.0*(rhoCMatrix.get(t1, t1)-1.0)*rhoB[t2][t1];
					for (int j=0; j<numTopicsS; j++)
					{
						if (j==t1) continue;
						rhoGrad[t1][t2]+=2.0*rhoCMatrix.get(t1, j)*rhoB[t2][j];
					}
				}
			}
		}
		for (int t1=0; t1<numTopicsS; t1++)
		{
			for (int t2=0; t2<numTopicsT; t2++)
			{
				gradient[t1*numTopicsT+t2]=-rhoGrad[t1][t2];
			}
		}
	}
	
	public int getNumParameters()
	{
		return numTopicsS*numTopicsT;
	}
	
	public double getParameter(int i)
	{
		int t1=i/numTopicsT,t2=i%numTopicsT;
		return rhoA[t1][t2];
	}
	
	public void getParameters(double buffer[])
	{
		for (int t1=0; t1<numTopicsS; t1++)
		{
			for (int t2=0; t2<numTopicsT; t2++)
			{
				buffer[t1*numTopicsT+t2]=rhoA[t1][t2];
			}
		}
	}
	
	public void setParameter(int i, double r)
	{
		int t1=i/numTopicsT,t2=i%numTopicsT;
		rhoA[t1][t2]=r;
	}
	
	public void setParameters(double newParameters[])
	{
		for (int t1=0; t1<numTopicsS; t1++)
		{
			for (int t2=0; t2<numTopicsT; t2++)
			{
				rhoA[t1][t2]=newParameters[t1*numTopicsT+t2];
			}
		}
	}
}