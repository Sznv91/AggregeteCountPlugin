/* Generated By:JJTree: Do not edit this line. ASTMulNode.java */

package com.tibbo.aggregate.common.expression.parser;

import com.tibbo.aggregate.common.expression.*;

public class ASTMulNode extends ExpressionNode
{
  public ASTMulNode(int id)
  {
    super(id);
  }
  
  public ASTMulNode(ExpressionParser p, int id)
  {
    super(p, id);
  }
  
  /** Accept the visitor. **/
  public AttributedObject jjtAccept(ExpressionParserVisitor visitor, EvaluationEnvironment data)
  {
    return visitor.visit(this, data);
  }
}
