/* Generated By:JJTree: Do not edit this line. ASTLogicalOrNode.java */

package com.tibbo.aggregate.common.expression.parser;

import com.tibbo.aggregate.common.expression.*;

public class ASTLogicalOrNode extends ExpressionNode
{
  public ASTLogicalOrNode(int id)
  {
    super(id);
  }
  
  public ASTLogicalOrNode(ExpressionParser p, int id)
  {
    super(p, id);
  }
  
  /** Accept the visitor. **/
  public AttributedObject jjtAccept(ExpressionParserVisitor visitor, EvaluationEnvironment data)
  {
    return visitor.visit(this, data);
  }
}