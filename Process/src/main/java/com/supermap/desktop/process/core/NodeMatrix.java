package com.supermap.desktop.process.core;

import com.supermap.desktop.process.events.*;

import javax.swing.event.EventListenerList;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by xie on 2017/3/13.
 * <pre>NodeMatrix store your node info(INodeConstraint),
 * EveryThing you like can be a node<pre/>
 */
public class NodeMatrix<T extends Object> {

	private Vector<T> nodes = new Vector();
	private Vector<Map<T, INodeConstraint>> matrix = new Vector<>();
	private EventListenerList listenerList = new EventListenerList();

	public NodeMatrix() {

	}

	/**
	 * Add node to nodes
	 *
	 * @param node
	 */
	public synchronized void addNode(T node) {
		Objects.requireNonNull(node);

		if (!this.nodes.contains(node)) {
			MatrixNodeAddingEvent<T> addingEvent = new MatrixNodeAddingEvent<T>(this, node, false);
			fireMatrixNodeAdding(addingEvent);

			if (!addingEvent.isCancel()) {
				this.nodes.add(node);
				this.matrix.add(new ConcurrentHashMap<T, INodeConstraint>());
				fireMatrixNodeAdded(new MatrixNodeAddedEvent<T>(this, node));
			}
		}
	}

	/**
	 * Remove formNode and toNode's constraint
	 *
	 * @param fromNode
	 * @param toNode
	 * @return If constraint remove success return true,
	 * else return false;
	 */
	public synchronized void removeNodeConstraint(T fromNode, T toNode) throws NodeException {
		validateNode(fromNode);
		validateNode(toNode);

		Map<T, INodeConstraint> nodeConstraints = matrix.get(this.nodes.indexOf(fromNode));
		if (nodeConstraints.containsKey(toNode)) {
			clearConstraint(nodeConstraints.get(toNode));
			nodeConstraints.remove(toNode);
		}
	}

	/**
	 * Remove node from matrix;
	 *
	 * @param node
	 * @return If node remove success return true,
	 * else return false;
	 */
	public synchronized void removeNode(T node) {
		validateNode(node);

		MatrixNodeRemovingEvent<T> removingEvent = new MatrixNodeRemovingEvent<>(this, node, false);
		fireMatrixNodeRemoving(removingEvent);

		if (removingEvent.isCancel()) {
			return;
		}

		// remove values
		int index = this.nodes.indexOf(node);

		for (int i = 0, size = this.matrix.size(); i < size; i++) {
			Map<T, INodeConstraint> map = this.matrix.get(i);

			if (map.containsKey(node)) {
				clearConstraint(map.get(node));
				map.remove(node);
			}
		}

		Map<T, INodeConstraint> nodeConstraints = this.matrix.get(index);
		for (Map.Entry<T, INodeConstraint> entry :
				nodeConstraints.entrySet()) {
			clearConstraint(entry.getValue());
		}

		// remove node
		this.matrix.get(index).clear();
		this.matrix.remove(index);
		this.nodes.remove(index);
		fireMatrixNodeRemoved(new MatrixNodeRemovedEvent<T>(this, node));
	}

	/**
	 * Get free nodes exiting in matrix;
	 *
	 * @return
	 */
	public synchronized Vector<T> getFreeNodes() {
		Vector<T> freeNodes = new Vector<>();

		for (int i = 0, size = this.nodes.size(); i < size; i++) {
			T node = this.nodes.get(i);

			if (!isRelatedFormSomeone(node) && !isRelateToAnyone(node)) {
				freeNodes.add(node);
			}
		}
		return freeNodes;
	}

	/**
	 * Returns <tt>true<tt/> if the specified node relates to any other node,<tt>false</tt> otherwise.
	 *
	 * @param node
	 * @return
	 */
	public synchronized boolean isRelateToAnyone(T node) {
		validateNode(node);

		int index = this.nodes.indexOf(node);
		return this.matrix.get(index).size() > 0;
	}

	/**
	 * Returns <tt>true<tt/> if the specified node is related from some other nodes,<tt>false</tt> otherwise.
	 *
	 * @param node
	 * @return
	 */
	public synchronized boolean isRelatedFormSomeone(T node) {
		validateNode(node);
		boolean ret = false;

		for (int i = 0, size = this.nodes.size(); i < size; i++) {
			if (isRelateTo(this.nodes.get(i), node)) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	/**
	 * Returns <tt>true</tt> if the specified fromNode relates to the specified toNode,<tt>false</tt> otherwise.
	 *
	 * @param fromNode
	 * @param toNode
	 * @return
	 */
	public synchronized boolean isRelateTo(T fromNode, T toNode) {
		validateNode(fromNode);
		validateNode(toNode);

		int fromIndex = this.nodes.indexOf(fromNode);
		Map<T, INodeConstraint> map = this.matrix.get(fromIndex);
		return map.containsKey(toNode);
	}

	private void validateNode(T node) {
		Objects.requireNonNull(node);
		if (!this.nodes.contains(node)) {
			throw new UnsupportedOperationException("Node not exits");
		}
	}

	/**
	 * If node has not previous or next node return true,else return false;
	 *
	 * @param node
	 * @return
	 */
	public synchronized boolean isFreeNode(T node) {
		return !isRelateToAnyone(node) && !isRelatedFormSomeone(node);
	}

	public synchronized boolean isLeadingNode(T node) {
		return isLeadingNode(node, false);
	}

	public synchronized boolean isLeadingNode(T node, boolean exceptFreeNodes) {
		validateNode(node);
		return exceptFreeNodes ? !isRelatedFormSomeone(node) && isRelateToAnyone(node) : !isRelatedFormSomeone(node);
	}

	/**
	 * 获得前一节点
	 * Get node's previous nodes
	 *
	 * @param node
	 * @return
	 */
	public synchronized Vector<T> getFromNodes(T node) {
		validateNode(node);

		Vector<T> fromNodes = new Vector<>();

		for (int i = 0, size = this.matrix.size(); i < size; i++) {
			Map<T, INodeConstraint> map = this.matrix.get(i);
			if (map.containsKey(node)) {
				fromNodes.add(node);
			}
		}
		return fromNodes;
	}

	/**
	 * Get node's previous nodes
	 *
	 * @param node
	 * @return If null menu that no such node
	 */
	public synchronized Vector<T> getToNodes(T node) {
		validateNode(node);

		Vector<T> toNodes = new Vector<>();

		Map<T, INodeConstraint> map = this.matrix.get(this.nodes.indexOf(node));
		for (T key :
				map.keySet()) {
			toNodes.add(key);
		}
		return toNodes;
	}

	/**
	 * Get all leading nodes
	 *
	 * @return
	 */
	public synchronized Vector<T> getLeadingNodes() {
		return getLeadingNodes(false);
	}

	/**
	 * Get all leading nodes
	 *
	 * @return
	 */
	public synchronized Vector<T> getLeadingNodes(boolean exceptFreeNodes) {
		Vector<T> leadingNodes = new Vector<>();

		for (int i = 0, size = this.nodes.size(); i < size; i++) {
			T node = this.nodes.get(i);

			if (!isRelatedFormSomeone(node)) {
				if (exceptFreeNodes) {
					if (isRelateToAnyone(node)) {
						leadingNodes.add(node);
					}
				} else {
					leadingNodes.add(node);
				}
			}
		}
		return leadingNodes;
	}

	/**
	 * Get all end nodes
	 *
	 * @return
	 */
	public synchronized Vector<T> getEndNodes() {
		return getEndNodes(false);
	}

	/**
	 * Get all end nodes
	 *
	 * @return
	 */
	public synchronized Vector<T> getEndNodes(boolean exceptFreeNodes) {
		Vector<T> endNodes = new Vector<>();

		for (int i = 0, size = this.nodes.size(); i < size; i++) {
			T node = this.nodes.get(i);

			if (!isRelateToAnyone(node)) {
				if (exceptFreeNodes) {
					if (isRelatedFormSomeone(node)) {
						endNodes.add(node);
					}
				} else {
					endNodes.add(node);
				}
			}
		}
		return endNodes;
	}

	/**
	 * Add constraint between two nodes
	 *
	 * @param fromNode
	 * @param toNode
	 * @param constraint
	 * @return If node1 can add constraint with node2
	 */
	public synchronized void addConstraint(T fromNode, T toNode, INodeConstraint constraint) {
		validateNode(fromNode);
		validateNode(toNode);

		Map<T, INodeConstraint> map = this.matrix.get(this.nodes.indexOf(fromNode));
		if (map.containsKey(toNode)) {
			clearConstraint(map.get(toNode));
		}

		map.put(toNode, constraint);
	}

	public synchronized INodeConstraint getConstraint(T fromNode, T toNode) {
		validateNode(fromNode);
		validateNode(toNode);

		Map<T, INodeConstraint> map = this.matrix.get(this.nodes.indexOf(fromNode));
		return map.containsKey(toNode) ? map.get(toNode) : null;
	}

	public synchronized Vector<T> getNodes() {
		Vector<T> nodes = new Vector<>();

		for (int i = 0, size = this.nodes.size(); i < size; i++) {
			nodes.add(this.nodes.get(i));
		}
		return nodes;
	}

	public synchronized int getCount() {
		return this.nodes.size();
	}

	private void clearConstraint(INodeConstraint constraint) {
		if (constraint != null) {
			constraint.clear();
		}
	}

	public void addMatrixNodeAddingListener(MatrixNodeAddingListener<T> listener) {
		this.listenerList.add(MatrixNodeAddingListener.class, listener);
	}

	private void removeMatrixNodeAddingListener(MatrixNodeAddingListener<T> listener) {
		this.listenerList.remove(MatrixNodeAddingListener.class, listener);
	}

	public void addMatrixNodeAddedListener(MatrixNodeAddedListener<T> listener) {
		this.listenerList.add(MatrixNodeAddedListener.class, listener);
	}

	public void removeMatrixNodeAddedListener(MatrixNodeAddedListener<T> listener) {
		this.listenerList.remove(MatrixNodeAddedListener.class, listener);
	}

	public void addMatrixNodeRemovingListener(MatrixNodeRemovingListener<T> listener) {
		this.listenerList.add(MatrixNodeRemovingListener.class, listener);
	}

	public void removeMatrixNodeRemovingListener(MatrixNodeRemovingListener<T> listener) {
		this.listenerList.remove(MatrixNodeRemovingListener.class, listener);
	}

	public void addMatrixNodeRemovedListener(MatrixNodeRemovedListener<T> listener) {
		this.listenerList.add(MatrixNodeRemovedListener.class, listener);
	}

	public void removeMatrixNodeRemovedListener(MatrixNodeRemovedListener<T> listener) {
		this.listenerList.remove(MatrixNodeRemovedListener.class, listener);
	}

	protected void fireMatrixNodeAdding(MatrixNodeAddingEvent<T> e) {
		Object[] listeners = this.listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i = i - 2) {
			if (listeners[i] == MatrixNodeAddingListener.class) {
				((MatrixNodeAddingListener<T>) listeners[i + 1]).matrixNodeAdding(e);
			}
		}
	}

	protected void fireMatrixNodeAdded(MatrixNodeAddedEvent<T> e) {
		Object[] listeners = this.listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i = i - 2) {
			if (listeners[i] == MatrixNodeAddedListener.class) {
				((MatrixNodeAddedListener<T>) listeners[i + 1]).matrixNodeAdded(e);
			}
		}
	}

	protected void fireMatrixNodeRemoving(MatrixNodeRemovingEvent<T> e) {
		Object[] listeners = this.listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i = i - 2) {
			if (listeners[i] == MatrixNodeRemovingListener.class) {
				((MatrixNodeRemovingListener<T>) listeners[i + 1]).matrixNodeRemoving(e);
			}
		}
	}

	protected void fireMatrixNodeRemoved(MatrixNodeRemovedEvent<T> e) {
		Object[] listeners = this.listenerList.getListenerList();

		for (int i = listeners.length - 2; i >= 0; i = i - 2) {
			if (listeners[i] == MatrixNodeRemovedListener.class) {
				((MatrixNodeRemovedListener<T>) listeners[i + 1]).matrixNodeRemoved(e);
			}
		}
	}
}
