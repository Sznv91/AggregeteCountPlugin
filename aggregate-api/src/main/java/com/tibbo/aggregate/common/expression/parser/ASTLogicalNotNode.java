/* Generated By:JJTree: Do not edit this line. ASTLogicalNotNode.java */

package com.tibbo.aggregate.common.expression.parser;

import com.tibbo.aggregate.common.expression.*;

public class ASTLogicalNotNode extends ExpressionNode
{
  public ASTLogicalNotNode(int id)
  {
    super(id);
  }
  
  public ASTLogicalNotNode(ExpressionParser p, int id)
  {
    super(p, id);
  }
  
  /** Accept the visitor. **/
  public AttributedObject jjtAccept(ExpressionParserVisitor visitor, EvaluationEnvironment data)
  {
    return visitor.visit(this, data);
  }
}