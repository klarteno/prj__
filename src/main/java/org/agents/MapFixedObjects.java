package org.agents;

import java.io.Serializable;
import java.util.*;

import org.agents.markings.Color;
import org.agents.markings.Coordinates;
import org.agents.markings.SolvedStatus;

//replace with composition one MapFixedObjects and one with opearations
public final class MapFixedObjects implements Serializable {
        //tracked_marking_ids key is for mark_id ,values for index
        private HashMap<Integer, Integer> tracked_marking_ids;
        //markings_ids is for for mark_id values for boxes or ojects
        private HashMap<Integer, Serializable> markings_ids;

        public static int MAX_ROW = 0 ;
        public static int MAX_COL = 0 ;

        private static boolean[][] walls;

        //goals is not intended to use in the algorithms instead we store the goals in the box or agent object
        public static HashMap<Character, int[]> goals = new HashMap<>();

        //central place to hold agents and boxes
        //when  agents and boxes gets a final cell position , goal cell it gets stored here
        private static Box[] boxes_store;
        private static Agent[] agents_store;

        private static HashMap<Integer, Agent> agents_ids;
        private static HashMap<Integer, Box> boxes_ids;

        private static HashMap<Integer, ArrayDeque<Agent>> colors_of_agents;
        private static HashMap<Integer, ArrayDeque<Box>> colors_of_boxes;

        public static Set<Integer> getAllBoxesIds() {
                return new HashSet<>(boxes_ids.keySet());
        }

        public static Box getBoxByID(Integer box_id) {
                return  boxes_ids.get(box_id);
        }

        public static ArrayDeque<Agent> getAgentsByColor(Integer color_no) {
                return colors_of_agents.get(color_no);
        }

        public static ArrayDeque<Box> getBoxesIDsByColor(Integer color_no) {
                return colors_of_boxes.get(color_no);
        }

        //TO DO : make the hashmap for key_color:keySet
        public static ArrayDeque<Integer> getBoxesIDsByColor(Integer key_color, Set<Integer> keySet) {
                ArrayDeque<Box> boxes_ = colors_of_boxes.get(key_color);
                ArrayDeque<Integer> same_boxes = new ArrayDeque<>();
                Box box;
                while(!boxes_.isEmpty()){
                        box = boxes_.pop();
                        if(keySet.contains(box.getLetterMark())){
                                same_boxes.add(box.getLetterMark());
                        }
                }

                return same_boxes;
        }

        public static boolean[][] getWalls() {
                return walls;
        }

        public static void setWalls(boolean[][] walls_marks) {
                walls = walls_marks;
        }

    private Serializable getMovableObject(int movable_id) {
                return this.markings_ids.get(movable_id);
        }

        private static HashMap<Integer, ArrayDeque<Box>> updateBoxesByColor(){
                ArrayDeque<Box> boxes;
                HashMap<Integer,ArrayDeque<Box>> colors_of_boxes = new HashMap<>();
                //Color color;
                for(Box box:boxes_store){
                        if (box.getSolvedStatus() != SolvedStatus.GOAL_FINAL_SOLVED)
                                if(colors_of_boxes.containsKey(box.getColor())){
                                        colors_of_boxes.get(box.getColor()).add(box);
                                }else {
                                        boxes = new ArrayDeque<>();
                                        boxes.add(box);
                                        colors_of_boxes.put(box.getColor(),boxes) ;
                                }
                }

                return colors_of_boxes;
        }


        private static HashMap<Integer, ArrayDeque<Agent>> updateAgentsByColor(){
                ArrayDeque<Agent> agents;
                HashMap<Integer,ArrayDeque<Agent>> colors_of_agents = new HashMap<>();
                //Color color;
                for(Agent agent:agents_store){
                        if (agent.getSolvedStatus() != SolvedStatus.GOAL_FINAL_SOLVED)
                                if(colors_of_agents.containsKey(agent.getColor())){
                                        colors_of_agents.get(agent.getColor()).add(agent);
                                }else {
                                        agents = new ArrayDeque<>();
                                        agents.add(agent);
                                        colors_of_agents.put(agent.getColor(),agents) ;
                        }
                }

                return colors_of_agents;
        }


        public synchronized static  LinkedList<Agent> getAgents(){
                LinkedList<Agent> not_solved_agents = new LinkedList<>();

                for (Agent agent : agents_store) {
                        if(agent.getSolvedStatus() != SolvedStatus.GOAL_FINAL_SOLVED){
                                not_solved_agents.add(agent);
                        }
                }

                return not_solved_agents;
        }

        public synchronized static boolean setAgents(Agent[] agents){
                for (Agent agent : agents_store) {
                        int mark_id = agent.getNumberMark();
                        if(agent.getSolvedStatus() != SolvedStatus.GOAL_FINAL_SOLVED){
                                for (Agent new_agent : agents) {
                                        if (mark_id == new_agent.getNumberMark() && agent.getColor()== new_agent.getColor()){
                                                agent = new_agent;
                                        }
                                }
                        }
                        else {
                                System.out.println("#tried to replace a box with status solved");
                                return false;
                        }
                }

                colors_of_agents = updateAgentsByColor();

                return true;
        }


        public synchronized static Set<Box> getBoxes(){
                 Set<Box> not_solved_boxes = new HashSet<>();

                for (Box box : boxes_store) {
                        if(box.getSolvedStatus() != SolvedStatus.GOAL_FINAL_SOLVED){
                                not_solved_boxes.add(box);
                        }
                }

                return  not_solved_boxes;
        }

        public synchronized static boolean setBoxes(Box[] boxes){
                for (Box box : boxes_store) {
                        int mark_id = box.getLetterMark();
                        if(box.getSolvedStatus() != SolvedStatus.GOAL_FINAL_SOLVED){
                                for (Box new_box : boxes) {
                                        if (mark_id == new_box.getLetterMark() && box.getColor()== new_box.getColor()){
                                                box = new_box;
                                        }
                                }
                         }
                        else {
                                System.out.println("#tried to replace a box with status solved");
                                return false;
                        }
                }

                colors_of_boxes = updateBoxesByColor();

                return true;
        }


        public static void setMovables(Agent[] agents_, Box[] boxes_){
                boxes_store = boxes_;
                colors_of_boxes = updateBoxesByColor();

                agents_store = agents_;
                colors_of_agents = updateAgentsByColor();

                agents_ids = new HashMap<>(boxes_store.length);
                boxes_ids = new HashMap<>(agents_store.length);
                //Arrays.stream(agents_)
                for (Agent agent:agents_ ) {
                        agents_ids.put(agent.getNumberMark(), agent);
                }
                for (Box box:boxes_ ) {
                        boxes_ids.put(box.getLetterMark(), box);
                }
        }

        //set ups a varying lenght of agents and boxes
        public void setUpTrackedMovables(Agent[] agents, Box[] boxes ){
                int number_of_movables = agents.length + boxes.length;
                this.tracked_marking_ids = new HashMap<>(number_of_movables);
                this.markings_ids = new HashMap<>(number_of_movables);

                int index = 0;
                for (Agent agent : agents) {
                        tracked_marking_ids.put(agent.getNumberMark(), index++);
                        this.markings_ids.put(agent.getNumberMark(),agent);
                }
                for (Box box : boxes) {
                        tracked_marking_ids.put(box.getLetterMark(), index++);
                        this.markings_ids.put(box.getLetterMark(), box);
                }
        }

        public static Serializable getByMarkNo(int movable_id){
                if (agents_ids.containsKey(movable_id)) {
                       return agents_ids.get(movable_id);

                }else if (boxes_ids.containsKey(movable_id)) {
                       return boxes_ids.get(movable_id);

                }else{
                        throw new UnsupportedOperationException("unknown movable id request");
                }
        }

        public static ArrayDeque<Serializable>  getByMarkNo(int[] movable_ids){
                ArrayDeque<Serializable> arrayDeque = new ArrayDeque<>();
                for (int movable_id : movable_ids) {
                        if (agents_ids.containsKey(movable_id)) {
                                arrayDeque.add(agents_ids.get(movable_id));
                        } else if (boxes_ids.containsKey(movable_id)) {
                                arrayDeque.add(boxes_ids.get(movable_id));
                        } else {
                                throw new UnsupportedOperationException("unknown movable id request");
                        }
                }
                return arrayDeque;
        }

        public static int getNumerOfAgents(){
                return agents_store.length;
        }

        public static Agent getByAgentMarkId(int mark_id){
                return agents_ids.get(mark_id);
        }

        public static Box getByBoxMarkId(int id){ return boxes_ids.get(id); }

        public static int getNumerOfBoxes() {
                return  boxes_store.length;
        }

        public static Set<Integer> getAllIdsMarks(){
                Set<Integer> keys = new HashSet<>(agents_ids.keySet());
                keys.addAll(boxes_ids.keySet());

                return keys;
        }

        public static Set<Integer> getAgentsMarks(){
                return new HashSet<>(agents_ids.keySet());
        }

        public static Set<Integer> getBoxesMarks(){
                return new HashSet<>(boxes_ids.keySet());
        }

        //this opearation is used the most in searching :TO DO replace with matrix look up
        //checks for cell free and compares also the time of boxes and agents
        public static boolean isFreeCell(int[] cell, int mark_id) {
                assert cell.length == 3;
                if(!isEmptyCell(cell))
                        return false;

                int movable_color = -1;

                if(agents_ids.containsKey(mark_id))
                        movable_color = agents_ids.get(mark_id).getColor();
                else{
                        if (boxes_ids.containsKey(mark_id)){
                                movable_color = boxes_ids.get(mark_id).getColor();
                        }else {
                                System.out.println("#mark_id: " +mark_id + " not found  ");
                        }
                }


                boolean isFreeCell = true;
                int y = Coordinates.getRow(cell);
                int x = Coordinates.getCol(cell);

                for(Agent next_agent: agents_store){
                        if(Arrays.equals(next_agent.getCoordinates(),cell)){
                                isFreeCell = false;
                                break;
                        }
                }

                for(Box next_box: boxes_store){
                        if(Arrays.equals(next_box.getCoordinates(),cell) && !(next_box.getColor() == movable_color)) {
                                isFreeCell = false;
                                break;
                        }
                }
                return isFreeCell;
        }

        public static boolean isEmptyCell(int[] cell_position){
                int row = Coordinates.getRow(cell_position);
                int col = Coordinates.getCol(cell_position);

                return (row > 0 && row < MAX_ROW  && col > 0 && col < MAX_COL && !getWalls()[row][col]);
        }

        public static LinkedList<int[]> getNeighboursMA(int[] position_to_expand, int time_deadline) {
                assert position_to_expand.length == 3;

                int time_step = Coordinates.getTime(position_to_expand);
                int row = Coordinates.getRow(position_to_expand);
                int col = Coordinates.getCol(position_to_expand);

                int[] dir_south = new int[]{time_step + 1, row+1, col };
                //if (!conflicts_avoidance.contains(dir_south) && isEmptyCell(dir_south)) neighbours_indexes.add(dir_south);
                int[] dir_north = new int[]{time_step+1, row-1, col};
                //if (!conflicts_avoidance.contains(dir_south) && isEmptyCell(dir_south)) neighbours_indexes.add(dir_south);
                int[] dir_east = new int[]{time_step + 1, row, col+1};
                //if (!conflicts_avoidance.contains(dir_south) && isEmptyCell(dir_south)) neighbours_indexes.add(dir_south);
                int[] dir_west = new int[]{time_step + 1, row, col-1};
                //if (!conflicts_avoidance.contains(dir_south) && isEmptyCell(dir_south)) neighbours_indexes.add(dir_south);

                int[] dir_wait = new int[]{time_step + 1, row, col};

                LinkedList<int[]> neighbours_valid = new LinkedList<>();
                if (isEmptyCell(dir_south))     neighbours_valid.add(dir_south);
                if (isEmptyCell(dir_north))     neighbours_valid.add(dir_north);
                if (isEmptyCell(dir_east))      neighbours_valid.add(dir_east);
                if (isEmptyCell(dir_west))      neighbours_valid.add(dir_west);
                if (Coordinates.getTime(dir_wait) <= time_deadline){
                        neighbours_valid.add(dir_wait);
                }

                return neighbours_valid;
        }

        //use roll unloop instead of java colection
        public static ArrayDeque<int[]> getNeighbours(int[] coordinates, int mark_id){
                assert coordinates.length == 3;
                ArrayDeque<int[]> neighbours_indexes = new ArrayDeque<>();

                int time_step = Coordinates.getTime(coordinates);
                int row = Coordinates.getRow(coordinates);   //y
                int col = Coordinates.getCol(coordinates);   //x


                int[] dir_south = new int[]{time_step+1, row+1, col };
                if (isFreeCell(dir_south, mark_id))
                        neighbours_indexes.add(dir_south);

                int[] dir_north = new int[]{time_step+1, row-1, col};
                if (isFreeCell(dir_north, mark_id))
                        neighbours_indexes.add(dir_north);

                int[] dir_east = new int[]{time_step+1, row, col+1};
                if (isFreeCell(dir_east, mark_id))
                        neighbours_indexes.add(dir_east);

                int[] dir_west = new int[]{time_step+1, row, col-1};
                if (isFreeCell(dir_west, mark_id))
                        neighbours_indexes.add(dir_west);

                return neighbours_indexes;
        }

        public static ArrayDeque<int[]> getNeighbours(int[] coordinates, Color color_movable){
                return getNeighbours(coordinates, Color.getColorCoded(color_movable));
        }

        public static Optional<Box> getNextBoxBy(int color) {
                for(Box next_box : boxes_store){
                        if(next_box.getColor() == color) {
                                return Optional.of(next_box);
                        }
                }
                return Optional.ofNullable(boxes_store[0]);
        }

        private Set<Integer> getTrackedMarkingsIDs() {
                return this.tracked_marking_ids.keySet();
        }

        private HashMap<Integer, Integer> getMarkingsIndexes() {
                return this.tracked_marking_ids;
        }

        public int getIndexFor(int movable_id) {
                 return this.tracked_marking_ids.get(movable_id);
        }

        public static int getManhattenHeuristic(int[] cell_coordinates, int[] goal_coordinates){
                return Math.abs(Coordinates.getRow(cell_coordinates) - Coordinates.getRow(goal_coordinates)) + Math.abs(Coordinates.getCol(cell_coordinates) - Coordinates.getCol(goal_coordinates))  ;
        }
}
