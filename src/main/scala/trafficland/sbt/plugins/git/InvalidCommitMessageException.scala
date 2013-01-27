package trafficland.sbt.plugins.git

case class InvalidCommitMessageException()
  extends Exception("git-commit requires a single, double-quoted commit message.")
