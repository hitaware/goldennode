package com.goldennode.api.cluster;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ClusteredMap<K, V> extends ClusteredObject implements Map<K, V> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public int size() {

		return 0;
	}

	@Override
	public boolean isEmpty() {

		return false;
	}

	@Override
	public boolean containsKey(Object key) {

		return false;
	}

	@Override
	public boolean containsValue(Object value) {

		return false;
	}

	@Override
	public V get(Object key) {

		return null;
	}

	@Override
	public V put(K key, V value) {

		return null;
	}

	@Override
	public V remove(Object key) {

		return null;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {

	}

	@Override
	public void clear() {

	}

	@Override
	public Set<K> keySet() {

		return null;
	}

	@Override
	public Collection<V> values() {

		return null;
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {

		return null;
	}/*
	 * extends ClusteredObject implements Map < K , V > {
	 *
	 * private static final long serialVersionUID = 3147877296085123335L ;
	 * private ConcurrentHashMap < K , V > innerMap = new ConcurrentHashMap < K
	 * , V > ( ) ;
	 *
	 * public ClusteredMap ( String publicName , String ownerId , Cluster
	 * cluster ) throws ClusterException { super ( publicName , ownerId ,
	 * cluster ) ; }
	 *
	 * public ClusteredMap ( String publicName , String ownerId ) throws
	 * ClusterException { super ( publicName , ownerId ) ; }
	 *
	 * @ Override public boolean isEmpty ( ) { return innerMap . isEmpty ( ) ; }
	 *
	 * @ Override public int size ( ) {
	 *
	 * return innerMap . size ( ) ; }
	 *
	 * @ Override public V put ( K k , V v ) {
	 *
	 * try { if ( getCluster ( ) != null ) {
	 *
	 * getCluster ( ) . safeMulticast ( new Operation ( getPublicName ( ) ,
	 * "put" , k , v ) ) ; } return _put ( k , v ) ; } catch ( ClusterException
	 * e ) { Logger . error ( e ) ; return null ; }
	 *
	 * }
	 *
	 * public V _put ( K k , V v ) { return innerMap . put ( k , v ) ; }
	 *
	 * @ Override public V remove ( Object key ) { try { if ( getCluster ( ) !=
	 * null ) {
	 *
	 * getCluster ( ) . safeMulticast ( new Operation ( getPublicName ( ) ,
	 * "remove" , key ) ) ; } return _remove ( key ) ; } catch (
	 * ClusterException e ) { Logger . error ( e ) ; return null ; } }
	 *
	 * public V _remove ( Object key ) { return innerMap . remove ( key ) ; }
	 *
	 * @ Override public void clear ( ) { try { if ( getCluster ( ) != null ) {
	 *
	 * getCluster ( ) . safeMulticast ( new Operation ( getPublicName ( ) ,
	 * "clear" ) ) ; } _clear ( ) ; } catch ( ClusterException e ) { Logger .
	 * error ( e ) ; } }
	 *
	 * public void _clear ( ) { innerMap . clear ( ) ; }
	 *
	 * @ Override public Set < K > keySet ( ) {
	 *
	 * return innerMap . keySet ( ) ; }
	 *
	 * @ Override public Collection < V > values ( ) {
	 *
	 * return innerMap . values ( ) ; }
	 *
	 * @ Override public Set < Entry < K , V >> entrySet ( ) {
	 *
	 * return innerMap . entrySet ( ) ; }
	 *
	 * @ Override public boolean containsKey ( Object key ) { return innerMap .
	 * containsKey ( key ) ; }
	 *
	 * @ Override public boolean containsValue ( Object value ) { return
	 * innerMap . containsValue ( value ) ; }
	 *
	 * @ Override public V get ( Object key ) { return innerMap . get ( key ) ;
	 * }
	 *
	 * @ Override public void putAll ( Map < ? extends K , ? extends V > m ) {
	 * throw new UnsupportedOperationException ( ) ; }
	 */

}
