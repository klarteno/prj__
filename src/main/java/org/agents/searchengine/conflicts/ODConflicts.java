package org.agents.searchengine.conflicts;

import org.agents.markings.Coordinates;
import org.agents.planning.conflicts.dto.EdgeConflict;
import org.agents.planning.conflicts.dto.SimulationConflict;
import org.agents.planning.conflicts.dto.VertexConflict;
import org.agents.planning.constraints.PullConstraint;
import org.agents.searchengine.ODNodeStructure;
import org.agents.searchengine.SearchMAState;

import java.util.*;

//operator decomosition search conflicts
/*
    * 2 kind of constraints : one for boxes one for agents
      agent moves from next cell of box than : constraint pull on the box
    * */
/*if there is no box constrained to expand: expand only agents
 *if no more agents to expand left expand boxes and do not move them just increase time step
 * */
public class ODConflicts {
    private final int[] group_marks_ids;
    //when expanding a standard state the tree rooted at this standard node colects all the conflicts for this tree in standard_to_conflicts
    ArrayList<SimulationConflict> standard_node_conflicts;
    HashMap<Integer, ArrayList<SimulationConflict>> standard_to_conflicts = new HashMap<>();
    ODNodeStructure od_node_structure;

    public  ODConflicts(int[] groupMarksIds, ODNodeStructure odNodeStructure) {
        this.group_marks_ids = groupMarksIds;
        od_node_structure = odNodeStructure;
    }

    public boolean clear(){
            if(standard_to_conflicts.size()>0){
                standard_to_conflicts.clear();
                standard_node_conflicts.clear();

                return true;
            }else {

                return false;
            }
    }

    public ArrayList<SimulationConflict> setStandardToConflicts(int[] current_pos){
        Optional<int[]> root_node = od_node_structure.getIntermediateNodeRoot(current_pos);
        int pos_key;

        if(root_node.isPresent()){
            pos_key = Arrays.hashCode(root_node.get());
        }else {
            pos_key = Arrays.hashCode(current_pos);
        }

        ArrayList<SimulationConflict> _standard_node_conflicts = new ArrayList<>();
        standard_to_conflicts.put(pos_key, _standard_node_conflicts);

        return _standard_node_conflicts ;
    }



    public ArrayList<SimulationConflict> getStandardToConflicts(int[] current_pos){
        Optional<int[]> root_node = od_node_structure.getIntermediateNodeRoot(current_pos);
        int pos_key;

        if(root_node.isPresent()){
            pos_key = Arrays.hashCode(root_node.get());
        }else {
            pos_key = Arrays.hashCode(current_pos);
        }

        if(standard_to_conflicts.containsKey(pos_key)){
            standard_node_conflicts = standard_to_conflicts.get(pos_key);
            //Utils.logAppendToFile(logFileName, "standard_conflicts called line 162");
            //Utils.logAppendToFile(logFileName, current_state,444);
        }
        return standard_node_conflicts;
    }

    public void addConflict(int[] current_pos, ArrayList<SimulationConflict> standard_conflicts) {
        int pos_key = Arrays.hashCode(current_pos);
        standard_to_conflicts.put(pos_key, standard_conflicts);
    }

    public ArrayList<SimulationConflict> getConflicts(int[] prev_standard_state__) {
        int prev_standard_state_key = Arrays.hashCode(prev_standard_state__);
        return standard_to_conflicts.get(prev_standard_state_key);
    }

    public void setConflictsEdgeConflict(int index_movable, int time_coord, int index_to_expand, int[] pos_coordinates, int[] cell_pos_neighbour){
        ArrayList<SimulationConflict> standard_conflicts = getStandardToConflicts(pos_coordinates);

        int[] cell_pos_neighbour1;
        boolean found_e = false;
        if(standard_to_conflicts.size() > 0){
            for (SimulationConflict simulationConflict : standard_conflicts) {
                if (simulationConflict instanceof EdgeConflict) {
                    int mark_id_conflicted = simulationConflict.getMarkedId();
                    if (mark_id_conflicted == group_marks_ids[index_movable]) {
                        found_e = true;
                        cell_pos_neighbour1 = Coordinates.createCoordinates(time_coord, Coordinates.getRow(cell_pos_neighbour), Coordinates.getCol(cell_pos_neighbour));
                        int[] position_to_expand = Coordinates.getCoordinatesAt(index_to_expand, pos_coordinates);
                        Coordinates.setTime(position_to_expand, time_coord +1);
                        //position_to_expand1 = Coordinates.createCoordinates( time_coord +1, Coordinates.getRow(position_to_expand), Coordinates.getCol(position_to_expand));
                        ((EdgeConflict) simulationConflict).addConflictedEdge(group_marks_ids[index_to_expand], cell_pos_neighbour1, position_to_expand);
                    }
                }
            }
        }
        if (!found_e) {
            EdgeConflict edge_conflict_found = new EdgeConflict(group_marks_ids[index_movable]);
            cell_pos_neighbour1 = Coordinates.createCoordinates(time_coord, Coordinates.getRow(cell_pos_neighbour), Coordinates.getCol(cell_pos_neighbour));
            int[] position_to_expand = Coordinates.getCoordinatesAt(index_to_expand, pos_coordinates);
            Coordinates.setTime(position_to_expand, time_coord +1);
            //position_to_expand1 = Coordinates.createCoordinates( time_coord +1, Coordinates.getRow(position_to_expand), Coordinates.getCol(position_to_expand));
            ((EdgeConflict) edge_conflict_found).addConflictedEdge(group_marks_ids[index_to_expand], cell_pos_neighbour1, position_to_expand);//the time steps should be reversed

            standard_conflicts.add(edge_conflict_found);
        }
    }

    public void setConflictsVertexConflict(int index_movable, int index_to_expand, int[] pos_coordinates,  int[] cell_pos_neighbour){
        ArrayList<SimulationConflict> standard_conflicts = getStandardToConflicts(pos_coordinates);

        boolean found_v = false;

        if(standard_conflicts.size() > 0){
            for (SimulationConflict simulationConflict : standard_conflicts){
                if ( simulationConflict instanceof VertexConflict){
                    int mark_id_conflicted = simulationConflict.getMarkedId();
                    if(mark_id_conflicted == group_marks_ids[index_movable]){
                        found_v = true;
                        ((VertexConflict)simulationConflict).addConflictedCell(group_marks_ids[index_to_expand], cell_pos_neighbour);////the time steps should be increased
                    }
                }
            }
        }if (!found_v) {
            VertexConflict vertex_conflict_found = new VertexConflict(group_marks_ids[index_movable]);
            vertex_conflict_found.addConflictedCell(group_marks_ids[index_to_expand], cell_pos_neighbour);////the time steps should be increased
            standard_conflicts.add(vertex_conflict_found);
        }
    }

    public ArrayDeque<int[]> getConflictsAvoidance(int index_to_expand, int[] pos_coordinates){
        ArrayList<SimulationConflict> standard_conflicts = getStandardToConflicts(pos_coordinates);
        int mark_id = group_marks_ids[index_to_expand];

        ArrayDeque<int []> conflicts_avoidance = new ArrayDeque<>();
        for ( SimulationConflict simulationConflict : standard_conflicts ){
            if(simulationConflict.getMarkedId() == mark_id ){
                ArrayList<int[]> coord = simulationConflict.getCoordinatesToAvoid();
                conflicts_avoidance.addAll(coord);
            }
        }
        return conflicts_avoidance;
    }

    public ArrayDeque<int[]> getConstraintMoving(int coord_movable ,int[] pos_coordinates){
        ArrayList<SimulationConflict> standard_conflicts = getStandardToConflicts(pos_coordinates);
        int mark_id = group_marks_ids[coord_movable];

        ArrayDeque<int[]> constraint_moving = new ArrayDeque<>();
        for ( SimulationConflict simulationConflict  : standard_conflicts ){
            if(simulationConflict.getMarkedId() == mark_id ){
                if (simulationConflict instanceof PullConstraint){
                    constraint_moving.addAll(((PullConstraint) simulationConflict).getConstraint_cells() );
                }
            }
        }

        return constraint_moving;
    }


    public void addPullConstraint(Integer box_index, int[] pos_coordinates, int[] pull_move_cell){
        ArrayList<SimulationConflict> standard_conflicts = getStandardToConflicts(pos_coordinates);

        //PullConstraint gets the cell set up expanded when the box index gets expanded in another node expansion
        //if a box has multiple PullConstraints then the node expansion expands all the PullConstraints making a new node for each of them
        boolean constraint_found = false;
        for (SimulationConflict simulationConflict : standard_conflicts){
            if(simulationConflict.getMarkedId() == group_marks_ids[box_index] && simulationConflict instanceof PullConstraint){
                ((PullConstraint) simulationConflict).addNextMoveCell(pull_move_cell);
                constraint_found = true;
            }
        }
        if(!constraint_found){
            PullConstraint pullConstraint = new PullConstraint(box_index, group_marks_ids[box_index]);
            pullConstraint.addNextMoveCell(pull_move_cell);
            standard_conflicts.add(pullConstraint);
        }
    }

    public Set<Integer> getCoordCandidates1(int coordinate_movable, int[] pos_coordinates){
        ArrayList<SimulationConflict> standard_conflicts = getStandardToConflicts(pos_coordinates);
        Set<Integer> coord_candidates = new HashSet<>();

        //if coordinate time does not corespond to a box with constraint : PullConstraint, VertexConflict, EdgeConflict then do not add candidates for exapansion
        int mark_id = group_marks_ids[coordinate_movable];
        boolean non_waiting_found = true;
        for (SimulationConflict simulationConflict : standard_conflicts ){
            if( (simulationConflict instanceof PullConstraint || simulationConflict instanceof EdgeConflict) && simulationConflict.getMarkedId() == mark_id ){
                coord_candidates.add(coordinate_movable);
                non_waiting_found = false;
            }
        }
        return coord_candidates;
    }
}
/*
//check if the boxes to avoid were overstepped in the new step time
        for(int[] neighbour_found : neighbours_agent){
            Set<Integer> keys = boxes_coord_to_avoid.keySet();
            /////////////////TO DO boxes_coord_to_avoid removed
            for(Integer key_avoid : boxes_coord_to_avoid.keySet()){
/////////////////////TO DO boxes_coord_to_avoid removed
                int[] box_to_avoid = boxes_coord_to_avoid.get(key_avoid);
                if( Coordinates.getRow(neighbour_found) == Coordinates.getRow(box_to_avoid) && Coordinates.getCol(neighbour_found) == Coordinates.getCol(box_to_avoid) ){
                    //check for PULL constraints
                    if(all_boxes_to_agents.containsKey(key_avoid)){
                        for (Integer key_agent : all_boxes_to_agents.get(key_avoid)){
                            //make  PULL constraints
                            int agent_time = Coordinates.getTime(key_agent, pos_coordinates);
                            int box_time_step = Coordinates.getTime(box_to_avoid);
                            //if the agent is next to box : add  PULL as state neighbours
                            //check if the agent has time steps left to pull the box and if yes add PULL constraint on box
                            int agent_row = Coordinates.getRow(key_agent, pos_coordinates);
                            int agent_col = Coordinates.getCol(key_agent, pos_coordinates);
                            if(agent_time < Coordinates.getTime(neighbour_found) && Coordinates.areNeighbours(box_to_avoid, agent_row, agent_col)) {
                                //make a pull constraint for the neighbour box
                                int[] pull_move_cell = new int[]{agent_time + 1, agent_row, agent_col };
                                od_conflicts.addPullConstraint(key_avoid, pos_coordinates, pull_move_cell);
                                //the PullConstraint imposes VertexConflicts for the other movables
                                setConflictsStandardStateExpansion(key_avoid, pos_coordinates, pull_move_cell);
                            }else{
                                neighbours_to_remove.add(neighbour_found);
                            }
                        }
                    }
                }
            }
        }


*/