/* Generated By:JJTree: Do not edit this line. ASTSubtractNode.java */

package com.tibbo.aggregate.common.expression.parser;

import com.tibbo.aggregate.common.expression.*;

public class ASTSubtractNode extends ExpressionNode
{
  public ASTSubtractNode(int id)
  {
    super(id);
  }
  
  public ASTSubtractNode(ExpressionParser p, int id)
  {
    super(p, id);
  }
  
  /** Accept the visitor. **/
  public AttributedObject jjtAccept(ExpressionParserVisitor visitor, EvaluationEnvironment data)
  {
    return visitor.visit(this, data);
  }
}