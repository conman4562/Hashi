import java.util.HashMap;
import java.util.Objects;

public class Island {
	
	public int row;
	public int col;
	public int value;
	public HashMap<Island, Integer> nb;
	
	public Island(int row, int col, int value) {
		this.row = row;
		this.col = col;
		this.value = value;
		nb = new HashMap<Island, Integer>();
	}
	
	public int totalNeighborValue() {
		int totalNeighborValue = 0;
		for (int i: nb.values()) {
			totalNeighborValue += i;
		}
		return totalNeighborValue;
	}
	
	public boolean isFull() {
		int totalNeighborValue = 0;
		for (int i: nb.values()) {
			totalNeighborValue += i;
		}
		return value == totalNeighborValue;
	}
	
	public boolean hasTooMany() {
		int totalNeighborValue = 0;
		for (int i: nb.values()) {
			totalNeighborValue += i;
		}
		return value < totalNeighborValue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(col, row, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Island other = (Island) obj;
		return col == other.col && row == other.row && value == other.value;
	}
	
	public String toString() {
		return "Row: " + row + ", Col: " + col + ", Val: " + value;
	}
	
}
