package enumerations;

/*
 * A SchedulingState is given to each HolidayMessage.
 * When an employee asks for a new absence, the state of the HM is New.
 * When the planner decides to wait a little bit with his decision, he can change the state to InConsideration.
 * He can also approve or reject the hm.
 *
 * NOTE: The global state of a HolidayMessage can only be approved when a planner of every planning unit an employee is
 * part of, has approved. To realise that, PlanningUnitStates are used.
 */

public enum SchedulingState {
    Approved, Rejected, InConsideration, New
}
