/**
 * Copyright (c) Cohesive Integrations, LLC
 * Copyright (c) Codice Foundation
 *
 * This is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or any later version. 
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. A copy of the GNU Lesser General Public License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 * 
 **/
package net.di2e.ecdr.commons.query.util.keywordparser;

/**
 * ASTNode that holds a boolean operator.
 */
public class OperatorASTNode extends ASTNode {
    private final Operator operator;

    public OperatorASTNode( Operator op, ASTNode left, ASTNode right ) {
        super(left, right);
        this.operator = op;
    }

    public OperatorASTNode( String op, ASTNode left, ASTNode right ) {
        super(left, right);
        this.operator = Operator.getOperatorFromString( op );
    }

    @Override
    public String getKeyword() {
        return null;
    }

    @Override
    public ASTNode.Operator getOperator() {
        return operator;
    }

    @Override
    public boolean isKeyword() {
        return false;
    }

    @Override
    public boolean isOperator() {
        return true;
    }

    @Override
    public boolean isPhraseStartDelimiter() {
        return false;
    }

    @Override
    public String toString() {
        return "Operator: " + operator.toString();
    }
}
