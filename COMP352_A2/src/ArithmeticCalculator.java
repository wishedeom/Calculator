import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/**
 * The StackArithmeticCalculator class contains a static method calculate() which takes an arithmetic expression as a string and returns the value of the expression, whether it is a real number
 * (represented as a double-precision floating-point number) or a boolean value. It is implemented using one stack for operands and another for operators.
 * @author Michael Deom
 *
 */
public class ArithmeticCalculator
{	
	private static Stack<Object> operands;		// Stacks hold the operands and operators
	private static Stack<Operator> operators;
	
//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
//
//
//	STACK CALCULATOR
//
//
//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Computes the value of an arithmetic expression.
	 * @param expression An arithmetic expression, expressed as a syntactically correct string, including no spaces. Supported operators are +, -, *, /, ^, !, <, >, <=, >=, ==, and !=.
	 * @return The value of the arithmetic expression. The value itself may be a real number or a boolean.
	 */
	public static Object stackCalculate(String expression)
	{
		expression = expression.replaceAll("\\s+","");	// Remove all spaces
		
		if (expression == null)
		{
			throw new IllegalArgumentException ("Input string cannot be null");
		}
		
		operands = new Stack<>();
		operators = new Stack<>();
		int symbolLength;												// Will hold the length of each operand/operator symbol as it is parsed
		for (int i = 0; i < expression.length(); i += symbolLength)
		{
			if (isNumberToken(expression, i))					// If the symbol starts with a digit or decimal place, the symbol is an operand
			{
				final String currentValue = getNumberToken(expression, i);
				final Double doubleValue = Double.valueOf(currentValue);
				symbolLength = currentValue.length();
				operands.push(doubleValue);							// Operands are placed on the stack
			}
			else
			{
				final Operator currentOperator = Operator.getOperatorToken(expression, i);	// Otherwise, the symbol is an operator/grouping symbol
				symbolLength = currentOperator.toString().length();
				if (!currentOperator.isGrouper())
				{
					flushToLowerEvaluationOrder(currentOperator);						// If the operator is not a parenthesis, all operators of lower evaluation order which are already on the stack
				}																		// are evaluated, before placing the current operator on the stack
				else if (currentOperator == Operator.OPEN_PARENTHESIS)
				{
					operators.push(currentOperator);									// Open parentheses are always placed on the stack
				}
				else if (currentOperator == Operator.CLOSE_PARENTHESIS)					// If the operator is a closing parenthesis, all operators on the stack are evaluated until an open parenthesis
				{																		// is encountered
					flushToOpenParenthesis();
				}
			}
		}
		flush();	// When the input has been finished parsing, all operators are evaluated
		
		if (operands.isEmpty())
		{
			throw new IllegalArgumentException ("Input expression cannot be empty.");
		}
		return operands.peek();		// What remains on the operand stack is the result
	}

	/**
	 * Extracts a floating-point number, as a string, from an arithmetic expression, ending at an operator.
	 * @param startIndex The string index at which the number begins. Throws an exception if this is not a digit or a decimal point.
	 * @param input The string from which a number is to be extracted.
	 * @return The extracted number, as a string.
	 */
	public static String getNumberToken(final String expression, final int startIndex)
	{		
		int endIndex = startIndex;
		do
		{
			endIndex++;
		}
		while (endIndex < expression.length() && isDigitOrDecimalPoint(expression.charAt(endIndex)));
		return expression.substring(startIndex, endIndex);
	}
	
	
	public static boolean isNumberToken(String expression, int startIndex)
	{
		return isDigitOrDecimalPoint(expression.charAt(startIndex))
				|| (expression.charAt(startIndex) == '-' && isDigitOrDecimalPoint(expression.charAt(startIndex + 1))
					&& (startIndex == 0 || !isNumberToken(expression, startIndex - 1))); 
	}
	
	/**
	 * Checks if a given character is a decimal digit or a decimal point.
	 * @param c The character to check.
	 * @return True if and only if the character is a decimal digit or a decimal point.
	 */
	public static boolean isDigitOrDecimalPoint(final char c)
	{
		return ('0' <= c && c <= '9') || c == '.';
	}
	
	/**
	 * Evaluates all operators currently on the stack.
	 */
	public static void flush()
	{
		while (!operators.isEmpty())
		{
			final Operator currentOperator = operators.pop();
			operands.push(applyToNextOperands(currentOperator));
		}
	}
	
	/**
	 * Evaluates operators on the stack until one of lower evaluation order is encountered.
	 * @param currentOperator The operator to compare evaluation order to
	 */
	public static void flushToLowerEvaluationOrder(final Operator currentOperator)
	{
		while (!operators.isEmpty() && currentOperator.evaluatedAfter(operators.peek()))
		{
			final Operator nextOperator = operators.pop();
			operands.push(applyToNextOperands(nextOperator));
		}
		operators.push(currentOperator);	// At the end, the current operator is placed at the top of the stack
	}
	
	/**
	 * Evaluates operators until an open parenthesis is encountered.
	 */
	public static void flushToOpenParenthesis()
	{
		while (!operators.isEmpty() && operators.peek() != Operator.OPEN_PARENTHESIS)
		{
			final Operator nextOperator = operators.pop();
			operands.push(applyToNextOperands(nextOperator));
		}
		operators.pop();	// At the end, removes the open parenthesis from the stack.
	}
	
	/**
	 * Applies an operator to the next valid operands on the stack.
	 * @param operator The operator to apply.
	 * @return The value of the operation, as a String. May represent a double or a boolean.
	 */
	public static Object applyToNextOperands(final Operator operator)
	{
		final double y = (Double) operands.pop();	// The first operand
		Object z;									// To hold output
		if (operator.isBinary())
		{			
			final double x = (Double) operands.pop();	// A binary operator requires another operand
			z = Operator.apply(operator, x, y);
		}
		else
		{
			z = Operator.apply(operator, y);
		}
		return z;
	}
	
	/**
	 * Takes arithmetic expressions from a given file, and outputs the expression and the value of the expression into another file.
	 * @param inputFilename The file to read from.
	 * @param outputFilename The file to write to.
	 * @param overwrite True if and only if the method may overwrite the input file. 
	 * @throws FileNotFoundException If the input file name is not found.
	 */
	public static void calculateFromFile(final String inputFilename, final String outputFilename, final boolean overwrite) throws FileNotFoundException
	{	
		if (!overwrite && inputFilename.equals(outputFilename))	// For safety; don't want to accidentally kill my input file!
		{
			throw new IllegalArgumentException("Overwrite flag off - cannot overwrite input file.");
		}
		
		final Scanner fileScanner = new Scanner(new FileInputStream(inputFilename));	// Read from input
		final PrintWriter outputStream = new PrintWriter(outputFilename);				// Write to output
		
		while (fileScanner.hasNextLine())	// Read each line, print input again, then write the output expression
		{
			final String inputLine = fileScanner.nextLine();
			outputStream.println("Input:  " + inputLine);
			Object outputLine;
			try
			{
				outputLine = stackCalculate(inputLine);
			}
			catch (Exception e)
			{
				outputLine = "Error: " + e.getMessage();	// Malformed input strings will not crash the program, only output an error for that line
			}
			outputStream.println("Output: " + outputLine);
			outputStream.println();
		}
		fileScanner.close();
		outputStream.close();
	}
	
	/**
	 * As calculateFromFile, but default sets the overwrite flag off. See calculateFromFile(String, String, boolean).
	 * @param inputFilename The file to read from.
	 * @param outputFilename The file to write to. 
	 * @throws FileNotFoundException If the input file name is not found.
	 */
	public static void calculateFromFile(final String inputFilename, final String outputFilename) throws FileNotFoundException
	{
		calculateFromFile(inputFilename, outputFilename, false);
	}
	
	/**
	 * Demonstration. Reads from input.txt; writes to output.txt
	 */
	public static void demo()
	{
		try
		{
			calculateFromFile("input.txt", "output.txt");
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	//--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	//
	//
	//		RECURSIVE CALCULATOR
	//
	//
	//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
	
	/**
	 * Entry point. Transforms an arithmetic expression into its value.
	 * @param expression The expression to read.
	 * @return The value of the expression, either double or boolean.
	 */
	public static Object recursiveCalculate(final String expression)
	{
		final ArrayList<Object> tokens = tokenize(expression.replaceAll("\\s+","")); // Remove spaces and tokenize.
		return recursiveCalculate(tokens); // Call workhorse method.
	}
	
	/**
	 * Workhorse method. Turns a list of tokens (values and operators) representing an expression into the expression's value.
	 * @param expression The expression to read, as a list of tokens.
	 * @return The value of the expression, either double or boolean.
	 */
	public static Object recursiveCalculate(final List<Object> expression)
	{
		Object returnValue = null;
		if (expression.size() == 1)
		{
			returnValue = expression.get(0); // Base case; return the remaining value
		}
		else
		{
			final int openParenthesisIndex = findOpenParenthesis(expression);	// Look for parenthesized sub-expressions, compute them, and then continue the computation
			if (openParenthesisIndex > -1)
			{
				final int closeParenthesisIndex = findCloseParenthesis(expression);
				if (closeParenthesisIndex == -1 || closeParenthesisIndex < openParenthesisIndex)
				{
					throw new IllegalArgumentException ("Mismatched parentheses.");
				}
				final List<Object> newExpression = new ArrayList<>();
				newExpression.addAll(expression.subList(0, openParenthesisIndex));
				newExpression.add(recursiveCalculate(expression.subList(openParenthesisIndex + 1, closeParenthesisIndex)));
				newExpression.addAll(expression.subList(closeParenthesisIndex + 1, expression.size()));
				returnValue = recursiveCalculate(newExpression); //Recursive call
			}
			else
			{
				final int leastPrecedentOperatorIndex = findLeastPrecedentOperator(expression);	// Split the expression by the operator with lowest precedence, compute the two halves, then compute the final value
				final Operator leastPrecedentOperator = (Operator) expression.get(leastPrecedentOperatorIndex);
				if (leastPrecedentOperator.isBinary())
				{
					final Double x = (Double) recursiveCalculate(expression.subList(0, leastPrecedentOperatorIndex));
					final Double y = (Double) recursiveCalculate(expression.subList(leastPrecedentOperatorIndex + 1, expression.size()));
					returnValue =  Operator.apply(leastPrecedentOperator, x, y);
				}
				else
				{
					if (leastPrecedentOperator == Operator.NEGATION)
					{
						final Double y = (Double) recursiveCalculate(expression.subList(leastPrecedentOperatorIndex + 1, expression.size()));
						returnValue =  Operator.apply(leastPrecedentOperator, y);
					}
					else if (leastPrecedentOperator == Operator.FACTORIAL)
					{
						final Double x = (Double) recursiveCalculate(expression.subList(0, leastPrecedentOperatorIndex));
						returnValue =  Operator.apply(leastPrecedentOperator, x);
					}
				}
			}
		}
		return returnValue;
	}
	
	/**
	 * Finds the first open parenthesis and returns it's index.
	 * @param expression The token list to search.
	 * @return The index if found, -1 otherwise.
	 */
	public static int findOpenParenthesis(final List<Object> expression)
	{
		int index = -1;
		for (int i = 0; i < expression.size(); i++)
		{
			if (expression.get(i) == Operator.OPEN_PARENTHESIS)
			{
				index = i;
				break;
			}
		}
		return index;
	}
	
	/**
	 * Finds the last close parenthesis and returns it's index.
	 * @param expression The token list to search.
	 * @return The index if found, -1 otherwise.
	 */
	public static int findCloseParenthesis(final List<Object> expression)
	{
		int index = -1;
		for (int i = expression.size() - 1; i >=0; i--)
		{
			if (expression.get(i) == Operator.CLOSE_PARENTHESIS)
			{
				index = i;
				break;
			}
		}
		return index;
	}
	
	/**
	 * Finds the first operator with lowest precedence.
	 * @param expression The token list to search.
	 * @return The index of the operator, -1 if not found.
	 */
	public static int findLeastPrecedentOperator(final List<Object> expression)
	{
		return findOperatorWithPrecedence(expression, minPrecedence(expression));
	}
	
	/**
	 * Finds the first operator with a given precedence.
	 * @param expression The token list to search.
	 * @param precedence The precedence of the operator to find.
	 * @return The index of the operator, -1 if not found.
	 */
	public static int findOperatorWithPrecedence(final List<Object> expression, final int precedence)
	{
		int index = -1;
		for (int i = 0; i < expression.size(); i ++)
		{
			final Object token = expression.get(i);
			if (token.getClass() == Operator.class && ((Operator) token).getPrecedence() == precedence)
			{
				index = i;
				break;
			}
		}
		return index;
	}
	
	/**
	 * Finds the lowest precedence of all operators in an expression.
	 * @param expression The token list to search.
	 * @return The lowest precedence. Returns the maximum integer value if no operators are found.
	 */
	public static int minPrecedence(final List<Object> expression)
	{
		int minPrecedence = Integer.MAX_VALUE;
		for (int i = 0; i < expression.size(); i ++)
		{
			final Object token = expression.get(i);
			if (token.getClass() == Operator.class)
			{
				final int precedence = ((Operator) token).getPrecedence();
				if (precedence < minPrecedence)
				{
					minPrecedence = precedence; 
				}
			}
		}
		return minPrecedence;
	}
	
	/**
	 * Converts an arithmetic expression as a string into a list of tokens.
	 * @param expression The expression to tokenize.
	 * @return A list of tokens representing the expression.
	 */
	public static ArrayList<Object> tokenize(final String expression)
	{
		ArrayList<Object> tokens = new ArrayList<>();
		int symbolLength;	// Used to iterate properly to the next symbol
		for (int i = 0; i < expression.length(); i += symbolLength)
		{
			Object token;
			if (isNumberToken(expression, i))
			{
				final String numberToken = getNumberToken(expression, i);
				symbolLength = numberToken.length();
				token = Double.valueOf(numberToken);
			}
			else
			{
				token = Operator.getOperatorToken(expression, i);
				symbolLength = token.toString().length();
			}
			tokens.add(token);
		}
		return tokens;
	}
	
//-----------------------------------------------------------------------------------------------------------------------------
//	
//	
//	MAIN METHOD
//	
//	
//	-----------------------------------------------------------------------------------------------------------------------------
	
	public static void main (String[] args)
	{
		demo();
		System.out.println(recursiveCalculate("4!-5^2/1*3==-51"));
		System.out.println(recursiveCalculate("7/5+10<=9"));
		System.out.println(recursiveCalculate("8*7/9-3!*2^4"));
		System.out.println(recursiveCalculate("5-10"));
	}
}