/* Generated By:JJTree: Do not edit this line. ASTGTNode.java */

package com.tibbo.aggregate.common.expression.parser;

import com.tibbo.aggregate.common.expression.*;

public class ASTGTNode extends ExpressionNode
{
  public ASTGTNode(int id)
  {
    super(id);
  }
  
  public ASTGTNode(ExpressionParser p, int id)
  {
    super(p, id);
  }
  
  /** Accept the visitor. **/
  public AttributedObject jjtAccept(ExpressionParserVisitor visitor, EvaluationEnvironment data)
  {
    return visitor.visit(this, data);
  }
}