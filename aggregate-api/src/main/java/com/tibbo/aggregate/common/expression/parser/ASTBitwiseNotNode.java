/* Generated By:JJTree: Do not edit this line. ASTBitwiseNotNode.java */

package com.tibbo.aggregate.common.expression.parser;

import com.tibbo.aggregate.common.expression.*;

public class ASTBitwiseNotNode extends ExpressionNode
{
  public ASTBitwiseNotNode(int id)
  {
    super(id);
  }
  
  public ASTBitwiseNotNode(ExpressionParser p, int id)
  {
    super(p, id);
  }
  
  /** Accept the visitor. **/
  public AttributedObject jjtAccept(ExpressionParserVisitor visitor, EvaluationEnvironment data)
  {
    return visitor.visit(this, data);
  }
}
