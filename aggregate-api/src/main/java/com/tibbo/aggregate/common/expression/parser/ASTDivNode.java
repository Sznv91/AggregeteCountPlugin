/* Generated By:JJTree: Do not edit this line. ASTDivNode.java */

package com.tibbo.aggregate.common.expression.parser;

import com.tibbo.aggregate.common.expression.*;

public class ASTDivNode extends ExpressionNode
{
  public ASTDivNode(int id)
  {
    super(id);
  }
  
  public ASTDivNode(ExpressionParser p, int id)
  {
    super(p, id);
  }
  
  /** Accept the visitor. **/
  public AttributedObject jjtAccept(ExpressionParserVisitor visitor, EvaluationEnvironment data)
  {
    return visitor.visit(this, data);
  }
}