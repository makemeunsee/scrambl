package threading

// container class for a flag to cancel a task.
// a single instance of this class should be associated to a single cancellable task
class CancelBox { var cancelled = false }