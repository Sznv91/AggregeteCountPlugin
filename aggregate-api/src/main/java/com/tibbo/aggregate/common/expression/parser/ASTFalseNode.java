/* Generated By:JJTree: Do not edit this line. ASTFalseNode.java */

package com.tibbo.aggregate.common.expression.parser;

import com.tibbo.aggregate.common.expression.*;

public class ASTFalseNode extends ExpressionNode
{
  public ASTFalseNode(int id)
  {
    super(id);
  }
  
  public ASTFalseNode(ExpressionParser p, int id)
  {
    super(p, id);
  }
  
  /** Accept the visitor. **/
  public AttributedObject jjtAccept(ExpressionParserVisitor visitor, EvaluationEnvironment data)
  {
    return visitor.visit(this, data);
  }
}
