
public class Tree {
	private Token info;
	Tree left;
	Tree right;
	
	public Tree (Token info) {
		this.info = info;
		this.left = null;
		this.right = null;
	}
	
	public void set_info(Token info) {
		this.info = info;
	}
	
	public Token get_info() {
		return this.info;
	}
	
	public void set_left(Tree left) {
		this.left = left;
	}
	
	public void set_right(Tree right) {
		this.right = right;
	}
	
	public Tree get_left() {
		return this.left;
	}
	
	public Tree get_right() {
		return this.right;
	}
	
	public void print() {
		System.out.print("(" + this.info.getToken() + ",(");
		if(this.left != null) {
			this.left.print();
		} else {
			System.out.print(")");
		}
		System.out.print(",");
		if(this.right != null) {
			this.right.print();
		} else {
			System.out.print("()");
		}
		System.out.print(")");
	}
}