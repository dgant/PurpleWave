package Types.Requirements

class RequireNothing extends Requirement {
  override def isPossible(): Boolean = true
  override def meetMinimally() {}
  override def meetOptimally() {}
  override def meetOptionally() {}
}
