class MainClass
{
	def main(): int{
		writeln((new SecondClass()).test());
		#writeln(new FifthClass());
		writeln((new SecondClass()).arrayTest()[2]);
		2+3;
		return 23;
	}
}
class SecondClass{
	def test():string{
		return "ye ace";
	}
	def test2(x: boolean): boolean{
		var z: boolean; 
		var y: boolean;
		while(1){
			z = ((x && y && z) || (!false) || (!true) || (2 == new FifthClass().test2()));
		}
		z = (new ThirdClass()).method();
		return true;
	}
	def arrayTest(): int[]{
		var result:int[];
		var len: int;
		result = new int[100];
		result[2] = 23; 
		len = result.length;
		return result;
	}
}
class FourthClass{
	def test():int{
		var x: int;
		var y: int;
		x = (new SecondClass()).test3();
		x = this.test2();
		return 0;
	}
}
class FifthClass{
	var instance1: FourthClass;
	var instance2: NotExistingClass;
	def test2(): int{
		var x: int; 
		var y: int;
		x = instance1.test();
		y = instance1.test4();
		return 232;
	}
	def rvalueTests(): int{
		var x: int;
		var z: int;
		this = new FifthClass();
		x + 2 = z;
		return 0;
	}
}
class Round1 extends Round3{
	
}
class Round2 extends Round1{
	
}
class Round3 extends Round2{
	
}