/* Generated By:JJTree: Do not edit this line. ASTNENode.java */

package com.tibbo.aggregate.common.expression.parser;

import com.tibbo.aggregate.common.expression.*;

public class ASTNENode extends ExpressionNode
{
  public ASTNENode(int id)
  {
    super(id);
  }
  
  public ASTNENode(ExpressionParser p, int id)
  {
    super(p, id);
  }
  
  /** Accept the visitor. **/
  public AttributedObject jjtAccept(ExpressionParserVisitor visitor, EvaluationEnvironment data)
  {
    return visitor.visit(this, data);
  }
}
