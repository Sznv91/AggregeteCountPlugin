/* Generated By:JJTree: Do not edit this line. ASTUnsignedRightShiftNode.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.tibbo.aggregate.common.expression.parser;

import com.tibbo.aggregate.common.expression.*;

public class ASTUnsignedRightShiftNode extends ExpressionNode
{
  public ASTUnsignedRightShiftNode(int id)
  {
    super(id);
  }
  
  public ASTUnsignedRightShiftNode(ExpressionParser p, int id)
  {
    super(p, id);
  }
  
  /** Accept the visitor. **/
  public AttributedObject jjtAccept(ExpressionParserVisitor visitor, com.tibbo.aggregate.common.expression.EvaluationEnvironment data)
  {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=7acb63a4bdddb45a76ffbaf431bdace1 (do not edit this line) */
