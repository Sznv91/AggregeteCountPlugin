/* Generated By:JJTree: Do not edit this line. ASTBitwiseComplNode.java */

package com.tibbo.aggregate.common.expression.parser;

import com.tibbo.aggregate.common.expression.*;

public class ASTBitwiseComplNode extends ExpressionNode
{
  public ASTBitwiseComplNode(int id)
  {
    super(id);
  }
  
  public ASTBitwiseComplNode(ExpressionParser p, int id)
  {
    super(p, id);
  }
  
  /** Accept the visitor. **/
  public AttributedObject jjtAccept(ExpressionParserVisitor visitor, EvaluationEnvironment data)
  {
    return visitor.visit(this, data);
  }
}