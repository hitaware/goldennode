package com.goldennode.api.goldennodegrid;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import org.slf4j.LoggerFactory;

import com.goldennode.api.grid.GridException;

public class ReplicatedMemoryMap<K, V> extends DistributedObject implements Map<K, V> {
	private static final long serialVersionUID = 1L;
	static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ReplicatedMemoryMap.class);
	Hashtable<K, V> innerMap = new Hashtable<K, V>();

	public ReplicatedMemoryMap() {
		super();
	}

	public ReplicatedMemoryMap(String publicName) {
		super(publicName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V put(K key, V value) {
		if (getGrid() != null) {
			return (V) safeOperate(new Operation(getPublicName(), "put", key, value));
		} else {
			return _put(key, value);
		}
	}

	public V _put(K key, V value) {
		return innerMap.put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(Object key) {
		if (getGrid() != null) {
			return (V) safeOperate(new Operation(getPublicName(), "remove", key));
		} else {
			return _remove(key);
		}
	}

	public V _remove(Object key) {
		return innerMap.remove(key);
	}

	@Override
	public void clear() {
		if (getGrid() != null) {
			safeOperate(new Operation(getPublicName(), "clear"));
		} else {
			_clear();
		}
	}

	public void _clear() {
		innerMap.clear();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerMap.size();
			} catch (GridException e1) {
				throw new RuntimeException(e1);
			} finally {
				if (locked) {
					try {
						getGrid().unlockReadLock(this);
					} catch (GridException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		} else {
			return innerMap.size();
		}
	}

	@Override
	public boolean isEmpty() {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerMap.isEmpty();
			} catch (GridException e1) {
				throw new RuntimeException(e1);
			} finally {
				if (locked) {
					try {
						getGrid().unlockReadLock(this);
					} catch (GridException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		} else {
			return innerMap.isEmpty();
		}
	}

	@Override
	public boolean containsKey(Object key) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerMap.containsKey(key);
			} catch (GridException e1) {
				throw new RuntimeException(e1);
			} finally {
				if (locked) {
					try {
						getGrid().unlockReadLock(this);
					} catch (GridException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		} else {
			return innerMap.containsKey(key);
		}
	}

	@Override
	public boolean containsValue(Object value) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerMap.containsValue(value);
			} catch (GridException e1) {
				throw new RuntimeException(e1);
			} finally {
				if (locked) {
					try {
						getGrid().unlockReadLock(this);
					} catch (GridException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		} else {
			return innerMap.containsValue(value);
		}
	}

	@Override
	public V get(Object key) {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerMap.get(key);
			} catch (GridException e1) {
				throw new RuntimeException(e1);
			} finally {
				if (locked) {
					try {
						getGrid().unlockReadLock(this);
					} catch (GridException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		} else {
			return innerMap.get(key);
		}
	}

	@Override
	public Set<K> keySet() {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerMap.keySet();
			} catch (GridException e1) {
				throw new RuntimeException(e1);
			} finally {
				if (locked) {
					try {
						getGrid().unlockReadLock(this);
					} catch (GridException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		} else {
			return innerMap.keySet();
		}
	}

	@Override
	public Collection<V> values() {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerMap.values();
			} catch (GridException e1) {
				throw new RuntimeException(e1);
			} finally {
				if (locked) {
					try {
						getGrid().unlockReadLock(this);
					} catch (GridException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		} else {
			return innerMap.values();
		}
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		if (getGrid() != null) {
			boolean locked = false;
			try {
				getGrid().readLock(this);
				locked = true;
				return innerMap.entrySet();
			} catch (GridException e1) {
				throw new RuntimeException(e1);
			} finally {
				if (locked) {
					try {
						getGrid().unlockReadLock(this);
					} catch (GridException e1) {
						throw new RuntimeException(e1);
					}
				}
			}
		} else {
			return innerMap.entrySet();
		}
	}
}
