/* Generated By:JJTree: Do not edit this line. ASTIdNode.java */

package com.tibbo.aggregate.common.expression.parser;

import com.tibbo.aggregate.common.expression.*;

public class ASTIdNode extends ExpressionNode
{
  
  String name;
  
  public ASTIdNode(int id)
  {
    super(id);
  }
  
  public ASTIdNode(ExpressionParser p, int id)
  {
    super(p, id);
  }
  
  /** Accept the visitor. **/
  public AttributedObject jjtAccept(ExpressionParserVisitor visitor, EvaluationEnvironment data)
  {
    return visitor.visit(this, data);
  }
}
