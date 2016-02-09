/**
 * Represents operators.
 * @author Michael Deom
 *
 */

public enum Operator
{	
	OPEN_PARENTHESIS			(0, "(", false, false, true),	// Not really operators, but belong on the operator stack 
	CLOSE_PARENTHESIS			(0, ")", false, false, true),	//
	FACTORIAL					(7, "!", false, true, false),
	NEGATION					(6, "-", false, false, false),
	POWER						(5, "^", true, true, false),
	MULTIPLICATION				(4, "*", true, true, false),
	DIVISION					(4, "/", true, true, false),
	ADDITION					(3, "+", true, true, false),
	SUBTRACTION					(3, "-", true, true, false),
	GREATER_THAN				(2, ">", true, true, false),	//
	GREATER_THAN_OR_EQUAL_TO	(2, ">=", true, true, false),	//
	LESS_THAN					(2, "<", true, true, false),	// Return boolean values
	LESS_THAN_OR_EQUAL_TO		(2, "<=", true, true, false),	//
	EQUAL_TO					(1, "==", true, true, false),	//
	NOT_EQUAL_TO				(1, "!=", true, true, false);	//
	
	final private int precedence;
	final private String operatorString;
	final private boolean isBinary;
	final private boolean isLeftAssociative;
	final private boolean isGrouper;
	
	/**
	 * Constructor.
	 * @param precedence How strongly the operator binds to operators. An operator with higher precedence will be evaluated before one with a lower precedence.
	 * @param operatorString The symbol which represents the operator.
	 * @param isBinary The arity of the operator. False if the operator is unary. No meaning for groupers.
	 * @param isLeftAssociative True if the operator binds to the left, false if to the right. No meaning for groupers.
	 * @param isGrouper True if the operator is actually a parenthesis. 
	 */
	private Operator (final int precedence, final String operatorString, final boolean isBinary, final boolean isLeftAssociative, final boolean isGrouper)
	{
		this.precedence = precedence;
		this.operatorString = operatorString;
		this.isBinary= isBinary;
		this.isLeftAssociative = isLeftAssociative;
		this.isGrouper = isGrouper;
	}
	
	/**
	 * How strongly the operator binds to operators. An operator with higher precedence will be evaluated before one with a lower precedence.
	 * @return The precedence of the operator.
	 */
	public int getPrecedence()
	{
		return precedence;
	}
	
	/**
	 * The symbol which represents the operator.
	 * @return The operator's symbol.
	 */
	public String toString()
	{
		return operatorString;
	}
	
	/**
	 * Checks the arity of the the operator. No meaning for groupers.
	 * @return True if the operator is binary, false if it is unary. 
	 */
	public boolean isBinary()
	{
		return isBinary;
	}
	
	/**
	 * Which side of the operator is more strongly bound.
	 * For a left-associative operator @,
	 * 		x @ y @ z = (x @ y) @ z
	 * For a right-associative operator @,
	 * 		x @ y @ z = x @ (y @ z)
	 * @return True if the operator binds to the left, false if to the right. No meaning for groupers.
	 */
	public boolean isLeftAssociative()
	{
		return isLeftAssociative;
	}
	
	/**
	 * Is the operator a parenthesis?
	 * @return True if and only if the operator is a parenthesis. 
	 */
	public boolean isGrouper()
	{
		return isGrouper;
	}
	
	/**
	 * The effective evaluation order of the calling operator with respect to the operator parameter.
	 * Depends on the associativity of the calling operator, and the relative precedence of the two. 
	 * @param otherOperator The operator to be compared to.
	 * @return True if and only if the calling operator will be evaluated after the operator parameter.
	 */
	public boolean evaluatedAfter(final Operator otherOperator)
	{
		boolean evaluatedAfter;
		if (isLeftAssociative())
		{
			evaluatedAfter = getPrecedence() <= otherOperator.getPrecedence();
		}
		else
		{
			evaluatedAfter = getPrecedence() < otherOperator.getPrecedence();
		}
		return evaluatedAfter;
	}
	
	/**
	 * Extracts an operator symbol from an arithmetic expression.
	 * @param startIndex The string index at which the operator begins. Throws an exception if this is not an operator character.
	 * @param expression The string from which an operator is to be extracted.
	 * @return The operator, as a string.
	 */
	public static Operator getOperatorToken(final String expression, final int startIndex)
	{
		final char currentCharacter = expression.charAt(startIndex); 
		Operator operator = null;
		switch (currentCharacter)
		{
			case '(':
				operator = OPEN_PARENTHESIS;
				break;
			case ')':
				operator = CLOSE_PARENTHESIS;
				break;
			case '!':
				if (startIndex < expression.length() - 1 && expression.charAt(startIndex + 1) == '=')
				{
					operator = NOT_EQUAL_TO;
				}
				else
				{
					operator = FACTORIAL;
				} 
				break;
			case '-':
				if (startIndex <= 0 || (!ArithmeticCalculator.isNumberToken(expression, startIndex - 1) && (expression.charAt(startIndex - 1) != ')')))
				{
					operator = NEGATION;
				}
				else if (ArithmeticCalculator.isNumberToken(expression, startIndex - 1) || expression.charAt(startIndex - 1) == ')')
				{
					operator = SUBTRACTION;
				}
				break;
			case '^':
				operator = POWER;
				break;
			case '*':
				operator = MULTIPLICATION;
				break;
			case '/':
				operator = DIVISION;
				break;
			case '+':
				operator = ADDITION;
				break;
			case '>':
				if (expression.charAt(startIndex + 1) == '=')
				{
					operator = GREATER_THAN_OR_EQUAL_TO;
				}
				else
				{
					operator = GREATER_THAN;
				}
				break;
			case '<':
				if (expression.charAt(startIndex + 1) == '=')
				{
					operator = LESS_THAN_OR_EQUAL_TO;
				}
				else
				{
					operator = LESS_THAN;
				}
				break;
			case '=':
				if (expression.charAt(startIndex + 1) == '=')
				{
					operator = EQUAL_TO;
				}
				break;
			default:
				break;
		}
		
		if (operator == null)
		{
			throw new IllegalArgumentException("No pattern for " + currentCharacter);
		}
		
		return operator;
	}
	
	/**
	 * Applies a unary operator to its argument.
	 * @param operator The unary operator.
	 * @param x The argument.
	 * @return The value of the operator applied to its argument.
	 */
	public static Double apply(final Operator operator, final double x)
	{
		double value;
		switch (operator)
		{
			case FACTORIAL:
				int y = (int) x;
				if (y != x)
				{
					throw new IllegalArgumentException ("Factorial arguments must be intgeral.");
				}
				value = factorial(y);
				break;
			case NEGATION:
				value = -x;
				break;
			default:
				throw new IllegalArgumentException (operator + " is not a supported operation.");
		}
		return value;
	}
	
	/**
	 * 
	 * Applies a binary operator to its arguments.
	 * @param operator The binary operator.
	 * @param x The first argument.
	 * @param y The second argument.
	 * @return The value of the operator applied to its arguments.
	 */
	public static Object apply(final Operator operator, final double x, final double y)
	{
		boolean returnValueIsDouble = true;
		double doubleReturnValue = Double.MIN_VALUE;
		boolean booleanReturnValue = false;
		switch (operator)
		{
			case POWER:
				doubleReturnValue = power(x, y);
				break;
			case MULTIPLICATION:
				doubleReturnValue = x * y;
				break;
			case DIVISION:
				doubleReturnValue = x / y;
				break;
			case ADDITION:
				doubleReturnValue = x + y;
				break;
			case SUBTRACTION:
				doubleReturnValue = x - y;
				break;
			case GREATER_THAN:
				booleanReturnValue = x > y;
				returnValueIsDouble = false;
				break;
			case GREATER_THAN_OR_EQUAL_TO:
				booleanReturnValue = x >= y;
				returnValueIsDouble = false;
				break;
			case LESS_THAN:
				booleanReturnValue = x < y;
				returnValueIsDouble = false;
				break;
			case LESS_THAN_OR_EQUAL_TO:
				booleanReturnValue = x <= y;
				returnValueIsDouble = false;
				break;
			case EQUAL_TO:
				booleanReturnValue = x == y;
				returnValueIsDouble = false;
				break;
			case NOT_EQUAL_TO:
				booleanReturnValue = x != y;
				returnValueIsDouble = false;
				break;
			default:
				throw new IllegalArgumentException (operator + " is not a supported operation.");
		}
		
		Object returnValue;
		if (returnValueIsDouble) // The type of value returned depends on whether the operator returns doubles or booleans
		{
			returnValue = doubleReturnValue;
		}
		else
		{
			returnValue = booleanReturnValue;
		}
		return returnValue;
	}
	
	/**
	 * Computes an integer power of a number.
	 * @param x The base.
	 * @param y The exponent.
	 * @return The power.
	 */
	public static double power(double x, double y)
	{
		double returnValue;
		
		final int floorY = (int) y;
		if (floorY == y)
		{
			returnValue = power(x, floorY); // If the exponent is an integer, use the integral version
		}
		else
		{
			returnValue = Math.exp(y * Math.log(x)); // The definition of non-integral powers. Didn't have much luck implementing my own exponential and logarithm methods.
		}
		
		return returnValue;
	}
	
	public static double power(final double x, final int y)
	{
		if (y == 0 && x == 0)
		{
			throw new IllegalArgumentException ("0^0 is undefined.");
		}
		
		double value;
		if (y >= 0)
		{
			value = 1;
			for (int i = 1; i <= y; i++)
			{
				value *= x; // Iteration cheaper than recursion
			}
		}
		else
		{
			value = 1.0 / power (x, -y); // Definition of negative powers.
		}
		return value;
	}
	
	/**
	 * Computes the factorial of an integer.
	 * @param n The integer.
	 * @return The factorial of n.
	 */
	public static int factorial(final int n)
	{
		if (n < 0)
		{
			throw new IllegalArgumentException ("Factorial argument must be non-negative.");
		}
		
		int value = 1;
		for (int i = 1; i <= n; i++)
		{
			value *= i;
		}
		return value;
	}
}