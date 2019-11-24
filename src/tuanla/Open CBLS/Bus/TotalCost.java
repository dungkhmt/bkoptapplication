package myself;

import java.util.HashMap;

import localsearch.functions.sum.SumVar;
import localsearch.model.AbstractInvariant;
import localsearch.model.IFunction;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;

public class TotalCost extends AbstractInvariant implements IFunction{

	private int _len;
	private int _value;
	private int _minValue;
	private int _maxValue;
		
	private int[][]  _d;
	private VarIntLS[] _x;
	
	private LocalSearchManager _ls;
	private HashMap<VarIntLS, Integer> _map;
	private boolean _posted;

	
	public TotalCost(int[][] d, VarIntLS[] x) {
		_x = x;		
		_d = d;
		_len = x.length;
		_ls=_x[0].getLocalSearchManager();
		_posted = false;
		
		post();
	}
	
	private void post() {
		if(_posted) return;
		
		_posted = true;
		
		_map = new HashMap<VarIntLS, Integer>();
		
		for (int i=0; i<_x.length; i++) {
			_map.put(_x[i],i);
		}
		
		_ls.post(this);
		
	}

	@Override
	public int getMinValue() {
		return _minValue;
	}

	@Override
	public int getMaxValue() {		
		return _maxValue;
	}

	@Override
	public int getValue() {		
		return _value;
	}
	@Override
	public VarIntLS[] getVariables() {
		return _x;
	}

	@Override
	public int getAssignDelta(VarIntLS x, int val) {
		
		if(_map.get(x)==null || x.getValue() == val) return 0;
		
		
		if (_map.get(x) == 0) {
			return _d[val][_x[1].getValue()] - _d[_x[0].getOldValue()][_x[1].getValue()];
		}
		else if (_map.get(x) == _len - 1) {
			return (_d[_x[_len - 2].getValue()][val] - _d[_x[_len - 2].getValue()][_x[_len - 1].getValue()]) + 
				   (_d[val][0] - _d[_x[_len - 1].getValue()][0]);
		}
		else {
			int k = _map.get(x);
			return (_d[_x[k-1].getValue()][val] - _d[_x[k-1].getValue()][_x[k].getValue()]) + 
				   (_d[val][_x[k+1].getValue()] - _d[_x[k].getValue()][_x[k+1].getValue()]);
		}		
	}

	@Override
	public int getSwapDelta(VarIntLS x, VarIntLS y) {
		// TODO Auto-generated method stub
		if(_map.get(x)==null&&_map.get(y)==null) return 0;
		if(_map.get(y)==null&&_map.get(x)!=null) return getAssignDelta(x,y.getValue());
		if(_map.get(y)!=null&&_map.get(x)==null) return getAssignDelta(y, x.getValue());
		return 0;
	}
	
	@Override
	public void propagateInt(VarIntLS x, int val) {
		if(_map.get(x)==null) return;
		
		if (_map.get(x) == 0) {
			_value += _d[val][_x[1].getValue()] - _d[_x[0].getOldValue()][_x[1].getValue()];
		}
		else if (_map.get(x) == _len - 1) {
			_value += (_d[_x[_len - 2].getValue()][val] - _d[_x[_len - 2].getValue()][_x[_len - 1].getOldValue()]) + 
				   (_d[val][0] - _d[_x[_len - 1].getOldValue()][0]);
		}
		else {
			int k = _map.get(x);
			_value += (_d[_x[k-1].getValue()][val] - _d[_x[k-1].getValue()][_x[k].getOldValue()]) + 
				   (_d[val][_x[k+1].getValue()] - _d[_x[k].getOldValue()][_x[k+1].getValue()]);
		}	
	}

	@Override
	public void initPropagate() {
		_value = 0;
		_minValue = 0;
		
		for(int i=0;i<_len;i++)
		{			
			for (int j=0; j<_len; j++) {
				if (_d[i][j] > _maxValue) {
					_maxValue = _d[i][j];
				}
			}			
		}
		
		for (int i=0; i<_len - 1; i++) {
			_value += _d[_x[i].getValue()][_x[i+1].getValue()];
		}
		
		_value += _d[_x[_len - 1].getValue()][0];
	}

	@Override
	public LocalSearchManager getLocalSearchManager() {
		return _ls;
	}
	
	public String name(){
		return "TotalCost";
	}
	
	@Override
	public boolean verify() {
		// TODO Auto-generated method stub
		int nv=0;
		
		for (int i=0; i<_len - 1; i++) {
			nv += _d[_x[i].getValue()][_x[i+1].getValue()];
		}
		
		nv += _d[_x[_len - 1].getValue()][0];
		
		if(nv == _value) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static void main(String[] args)
	{
		LocalSearchManager ls=new LocalSearchManager();
		VarIntLS[] x=new VarIntLS[7];
		
		int[][] d = new int[][] {
			{0, 8, 5, 1, 10, 5, 9},
			{9, 0, 5, 6, 6, 2, 8},
			{2, 2, 0, 3, 8, 7, 2},
			{5, 3, 4, 0, 3, 2, 7},
			{9, 6, 8, 7, 0, 9, 10},
			{3, 8, 10, 6, 5, 0, 2},
			{3, 4, 4, 5, 2, 2, 0}
									};
		
		
		for(int i=0;i<x.length;i++)
		{
			x[i]=new VarIntLS(ls, 0, 6);
			x[i].setValue(i);
		}
		
		TotalCost s=new TotalCost(d, x);
		ls.close();
		
		System.out.println(s.getValue());
		System.out.println(s.getAssignDelta(x[1], 3));
		
		x[1].setValuePropagate(3);
		
		System.out.println("The new value of s: " + s.getValue());		
	}
}
