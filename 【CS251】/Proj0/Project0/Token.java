
public class Token {
	private int token;
	private String tokenValue_string;
	private float tokenValue_float;
	private int tokenValue_int;
	private char tokenValueType;
	private char type;
		
	public Token(int token, String value) {
		this.token = token;
		this.tokenValue_string = value;
		this.type = 's';
	}
	
	public Token(int token, float value) {
		this.token = token;
		this.tokenValue_float = value;
		this.type = 'f';
	}

	public Token(int token, int value) {
		this.token = token;
		this.tokenValue_int = value;
		this.type = 'i';
	}
	
	public String getValue_s() {
		return this.tokenValue_string;
	}
	
	public float getValue_f() {
		return this.tokenValue_float;
	}
	
	public int getValue_i() {
		return this.tokenValue_int;
	}
	
	public void setValue(int value) {
		this.tokenValue_int = value;
		this.tokenValueType = 'i';
	}
	
	public void setValue(float value) {
		this.tokenValue_float = value;
		this.tokenValueType = 'f';
	}
	
	public char getValueType() {
		return this.tokenValueType;
	}
	
	public int getToken() {
		return this.token;
	}
	
	public char getType() {
		return this.type;
	}
	
	public void print() {
		System.out.print("(" + this.token + ",");
		if(this.type == 'i') {
			System.out.print(this.tokenValue_int);
		}
		else if(this.type == 'f') {
			System.out.print(this.tokenValue_float);
		} else {
			System.out.print(this.tokenValue_string);
			if(this.getValueType() == 'i') {
				System.out.print(",tokenValue:" + this.tokenValue_int);
			}
			else if(this.getValueType() == 'f') {
				System.out.print(",tokenValue:" + this.tokenValue_float);
			}
		}
		System.out.print("," + this.getType() + ")");
	}
}
