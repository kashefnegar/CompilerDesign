class MainClass{
	def main(): int{
		writeln((new SecondClass()).test());
		new SecondClass();
		return 23;
	}
}
class SecondClass{
	def test(): MyClass{
		return new ThirdClass();
	}
}
class MyClass{var x: int;}
class ThirdClass extends MyClass{
	def test(): string{
		var y: int;
		y = x * x; 
		return this.test2();
	}
	def test2(): string {
		var x: string; 
		var z: int;
		var p: string;
		if (z == 0) then{
			p = x;
		}
		else {
			p = this.test3("salam");
		}
		return p;
	}
	def test3(input: string): string
	{
		return this.test4();
	}
	def test4(): int{ 
		return 232 * 456;
	}

	def test5(): int{
		var y: boolean;
		if (y == 2) then{
			y = false || y;
			new ThirdClass().test();
		} 
		return z;
	}
	def test6():int{
		var x: int[];
		var y: int[];
		var z: int;
		var a: int[];
		var f: int[];
		x = new int[10];
		y = new int[10];
		z = (x=y)[2];
		a = (x = y);
		f = (x = y)[2];
		return 2;
	}
	def methodCalls(): int{
		var x: int;
		var s: string;
		x = this.test6(x);
		s = this.test6();
		x = this.methodWithInput(s);
		return 0;
	}
	def methodWithInput(input: int): int{
		return input;
	}

}