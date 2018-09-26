package uk.ac.manchester.trafford.network;

import java.util.Objects;

public class Node {
	private String name;

	/**
	 * 
	 * @param name Unique name for the node.
	 */
	public Node(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null) {
			return false;
		}
		if (getClass() != o.getClass()) {
			return false;
		}
		Node node = (Node) o;

		return Objects.equals(name, node.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}
}
