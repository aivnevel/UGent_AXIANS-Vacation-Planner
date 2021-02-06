package enumerations;

/*
 * These states are used by the planner to check if a group of holidayMessages can be planned in.
 * If a holidayMessage has the CollisionState ALWAYS, it can not be planned in. A working planner could not be made.
 * If a holidayMessage has the CollisionState NEVER, it can always be planned in. It will never be a problem.
 * If a group of holidayMessages has the CollisionState SOMETIMES, the group or multiple subgroups form a colliding group
 *      One holidayMessage of a colliding group may not be planned in. e.g. when one person is needed on a certain day,
 *      there are two people that can do the shift and they both want to be absent, only one of both absences can be
 *      planned in.
 */
public enum CollisionState {
    ALWAYS, NEVER, SOMETIMES, UNKNOWN
}