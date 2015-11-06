/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.lucee.extension.pdf.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.dt.DateTime;

public abstract class StructSupport implements Map,Struct {

	private static final long serialVersionUID = 7433668961838400995L;
	private CFMLEngine engine;

	public StructSupport() {
		this.engine=CFMLEngineFactory.getInstance();
	}
	
	/**
	 * throw exception for invalid key
	 * @param key Invalid key
	 * @return returns an invalid key Exception
	 */
	public PageException invalidKey(Config config,Struct sct,Key key, String in) {
		String appendix=Util.isEmpty(in,true)?"":" in the "+in;
		Iterator<Key> it = sct.keyIterator();
		Key k;

		while(it.hasNext()){
			k = it.next();
			if( k.equals( key ) )
				return engine.getExceptionUtil().createExpressionException( "the value from key [" + key.getString() + "] "+appendix+" is NULL, which is the same as not existing in CFML" );
		}
		if(config==null)config=engine.getThreadConfig();
		return engine.getExceptionUtil().createExpressionException( "key [" + key.getString() + "] doesn't exist"+appendix);
	}

	@Override
	public Set entrySet() {
		throw notSupported();
		// TODO return StructUtil.entrySet(this);
	}


	@Override
	public final Object get(Object key) {
		return get(engine.getCastUtil().toKey(key,null), null);
	}

	@Override
	public final boolean isEmpty() {
		return size()==0;
	}

	@Override
	public Set keySet() {
		throw notSupported();
		// TODO return StructUtil.keySet(this,true);
	}

	@Override
	public Object put(Object key, Object value) {
		return setEL(engine.getCastUtil().toKey(key,null), value);
	}

	@Override
	public final void putAll(Map t) {
		throw notSupported();
		// TODO StructUtil.putAll(this, t);
	}

	@Override
	public final Object remove(Object key) {
		return removeEL(engine.getCastUtil().toKey(key,null));
	}

	@Override
	public Object remove(Collection.Key key,Object defaultValue) {
		try {
			return remove(key);
		} catch (PageException e) {
			return defaultValue;
		}
	}

	@Override
	public final Object clone(){
		return duplicate(true);
	}
	
	@Override
	public final boolean containsKey(Object key) {
		return containsKey(engine.getCastUtil().toKey(key,null));
	}

	@Override
	public final boolean containsKey(String key) {
		return containsKey(engine.getCastUtil().toKey(key));
	}

	@Override
	public final Object get(String key, Object defaultValue) {
		return get(engine.getCastUtil().toKey(key), defaultValue);
	}

	@Override
	public final Object get(String key) throws PageException {
		return get(engine.getCastUtil().toKey(key));
	}

	@Override
	public final Object set(String key, Object value) throws PageException {
		return set(engine.getCastUtil().toKey(key), value);
	}

	@Override
	public final Object setEL(String key, Object value) {
		return setEL(engine.getCastUtil().toKey(key), value);
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel,DumpProperties properties) {
		throw notSupported();
		// TODO return StructUtil.toDumpTable(this,"Struct",pageContext,maxlevel,properties);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
        throw engine.getExceptionUtil().createExpressionException("can't cast Complex Object Type Struct to a boolean value");
    }
    
    @Override
	public Boolean castToBoolean(Boolean defaultValue) {
        return defaultValue;
    }

    @Override
	public double castToDoubleValue() throws PageException {
        throw engine.getExceptionUtil().createExpressionException("can't cast Complex Object Type Struct to a number value");
    }
    
    @Override
	public double castToDoubleValue(double defaultValue) {
        return defaultValue;
    }

    @Override
	public DateTime castToDateTime() throws PageException {
        throw engine.getExceptionUtil().createExpressionException("can't cast Complex Object Type Struct to a Date");
    }
    
    @Override
	public DateTime castToDateTime(DateTime defaultValue) {
        return defaultValue;
    }

    @Override
	public String castToString() throws PageException {
        throw engine.getExceptionUtil().createExpressionException("Can't cast Complex Object Type Struct to String",
          "Use Built-In-Function \"serialize(Struct):String\" to create a String from Struct");
    }

    @Override
	public String castToString(String defaultValue) {
        return defaultValue;
    }

    @Override
	public int compareTo(boolean b) throws PageException {
		throw engine.getExceptionUtil().createExpressionException("can't compare Complex Object Type Struct with a boolean value");
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw engine.getExceptionUtil().createExpressionException("can't compare Complex Object Type Struct with a DateTime Object");
	}

	@Override
	public int compareTo(double d) throws PageException {
		throw engine.getExceptionUtil().createExpressionException("can't compare Complex Object Type Struct with a numeric value");
	}

	@Override
	public int compareTo(String str) throws PageException {
		throw engine.getExceptionUtil().createExpressionException("can't compare Complex Object Type Struct with a String");
	}

	@Override
	public java.util.Collection values() {
		throw notSupported();
		// TODO return StructUtil.values(this);
	}

	@Override
	public boolean containsValue(Object value) {
		return values().contains(value);
	}
	
    @Override
	public Iterator<String> keysAsStringIterator() {
    	throw notSupported();
		// TODO return new KeyAsStringIterator(keyIterator());
    }

    @Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		return get(key, defaultValue);
	}

    @Override
	public Object get(PageContext pc, Key key) throws PageException {
		return get(key);
	}

    @Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		return set(propertyName, value);
	}

    @Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		return setEL(propertyName, value);
	}

    @Override
	public Object call(PageContext pc, Key methodName, Object[] args) throws PageException {
		Object obj = get(methodName,null);
		if(obj instanceof UDF) {
			return ((UDF)obj).call(pc,methodName,args,false);
		}
		
		throw CFMLEngineFactory.getInstance().getExceptionUtil().createApplicationException("no function with name "+methodName+" found!");
		// TODO return MemberUtil.call(pc, this, methodName, args, CFTypes.TYPE_STRUCT, "struct");
	}

    @Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		Object obj = get(methodName,null);
		if(obj instanceof UDF) {
			return ((UDF)obj).callWithNamedValues(pc,methodName,args,false);
		}
		throw CFMLEngineFactory.getInstance().getExceptionUtil().createApplicationException("no function with name "+methodName+" found!");
		// TODO return MemberUtil.callWithNamedValues(pc,this,methodName,args, CFTypes.TYPE_STRUCT, "struct");
	}
    
    @Override
	public java.util.Iterator<?> getIterator() {
    	return keysAsStringIterator();
    } 

    /* TODO public boolean equals(Object obj){
		if(!(obj instanceof Collection)) return false;
		return CollectionUtil.equals(this,(Collection)obj);
	}*/

	private RuntimeException notSupported() {
		return CFMLEngineFactory.getInstance().getExceptionUtil().createPageRuntimeException(
				CFMLEngineFactory.getInstance().getExceptionUtil().createApplicationException("this method is not supported!")
				);
	}
}