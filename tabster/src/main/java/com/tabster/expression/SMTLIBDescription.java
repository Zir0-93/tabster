package com.tabster.expression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.tabster.SMTFunction;

/**
 * Processes expressions to build a SMT-Lib Standard v2.5 description, see
 * http://smtlib.cs.uiowa.edu/papers/smt-lib-reference-v2.5-r2015-06-28.pdf for
 * more details...
 *
 * @author Muntazir Fadhel
 */
public class SMTLIBDescription {

	private ArrayList<SMTFunction> expressionInputs;
	private boolean getModel;
	private boolean checkSat;

	public SMTLIBDescription(ArrayList<SMTFunction> expressionInputs, String originalTabularExpression, boolean checkSat, boolean getModel) {
		this.expressionInputs = expressionInputs;
		declareTerms(this.expressionInputs);
		this.smtLIBDescription += " (assert ";
		this.checkSat = checkSat;
		this.getModel = getModel;
	}

	public String getEndingString() {

		 String smtLibEndStr = ") ";

		if (checkSat) {
			smtLibEndStr += "(check-sat) ";
		}
		if (getModel) {
			smtLibEndStr += "(get-model) ";
		}
		smtLibEndStr += "(exit)";
		
		return smtLibEndStr;
	}
	
    /**
     * Maps common expression operators to their equivalent SMT LIB String
     * representation.
     */
    static final Map<String, String> SMT_LIB_OPERATOR_MAP = new HashMap<String, String>();
    static {
        SMT_LIB_OPERATOR_MAP.put("+", "+");
        SMT_LIB_OPERATOR_MAP.put("-", "-");
        SMT_LIB_OPERATOR_MAP.put("=", "=");
        SMT_LIB_OPERATOR_MAP.put("&", "and");
        SMT_LIB_OPERATOR_MAP.put("|", "or");
        SMT_LIB_OPERATOR_MAP.put("!", "not");
        SMT_LIB_OPERATOR_MAP.put("~", "not");
        SMT_LIB_OPERATOR_MAP.put("||", "or");
        SMT_LIB_OPERATOR_MAP.put("&&", "and");
        SMT_LIB_OPERATOR_MAP.put("*", "*");
        SMT_LIB_OPERATOR_MAP.put("/", "/");
        SMT_LIB_OPERATOR_MAP.put("==", "=");
        SMT_LIB_OPERATOR_MAP.put(">", ">");
        SMT_LIB_OPERATOR_MAP.put("<", "<");
        SMT_LIB_OPERATOR_MAP.put("<=", "<=");
        SMT_LIB_OPERATOR_MAP.put(">=", ">=");
        SMT_LIB_OPERATOR_MAP.put("%", "mod");
        SMT_LIB_OPERATOR_MAP.put("!=", "!=");
        SMT_LIB_OPERATOR_MAP.put("∀", "forall");
        SMT_LIB_OPERATOR_MAP.put("∃", "exists");
        SMT_LIB_OPERATOR_MAP.put("∧", "and");
        SMT_LIB_OPERATOR_MAP.put("∨", "or");
    }

   
    private static final String SMT_LIB_DESC_BEGIN = "(set-logic AUFLIRA) (set-option :produce-models true) ";

    private String smtLIBDescription = SMT_LIB_DESC_BEGIN;

    public String smtLIBDescription() {
        return smtLIBDescription + getEndingString();
    }
    
    @Override
    public String toString() {
    	return smtLIBDescription();
    }

    public void registerExpressionEnd() {
        smtLIBDescription += ") ";
    }

    public void registerNewTerm(String term) {
        smtLIBDescription += term + " ";
    }

    /**
     * Registers the start of a new sub expression within the tabular
     * expression.
     *
     * @param expressionOperatorStr
     *            operator used in the sub expression.
     */
    public void registerExpressionStart(final String expressionOperatorStr) {

        if (SMT_LIB_OPERATOR_MAP.containsKey(expressionOperatorStr)) {
            smtLIBDescription += "(" + SMT_LIB_OPERATOR_MAP.get(expressionOperatorStr) + " ";
        } else {
            throw new IllegalArgumentException(expressionOperatorStr
                    + " could not be processed in the tabular expression!");
        }
    }

    /**
     * @param variableName
     *            name of the new term to declare
     */
    public void declareTerms(ArrayList<SMTFunction> inputs) {
    	for (SMTFunction input : inputs) {
    		smtLIBDescription += "(declare-fun " + input.varName() + " () " + input.type().value() + ") ";   
    	}
    }

	public void registerPredicateExpressionStart(String quantifierSymbol,
			String variable) throws Exception {
		String type = null;
		for (SMTFunction possibleMatchingInput : expressionInputs) {
			if (possibleMatchingInput.varName().equals(variable)) {
				type = possibleMatchingInput.type().value();
			}
		}
		if (type == null) {
			throw new Exception("Could not translate predicate expression!");
		}
		registerExpressionStart(quantifierSymbol);
		smtLIBDescription += "((" + variable + " " + type + ")) "; 
	}
}
