package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeBase;
import ticket.booking.entities.Ticket;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.util.UserServiceUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserBookingService {
    private User user;
    private static final String USERS_PATH="../localdb/users.json";
    private List<User> userList;
    private ObjectMapper objectMapper=new ObjectMapper();

    public UserBookingService(User user) throws IOException {
        this.user=user;
        loadUserListFromFile();

    }
    public UserBookingService() throws IOException{
        loadUserListFromFile();

    }
    private void loadUserListFromFile() throws IOException{
        userList=objectMapper.readValue(new File(USERS_PATH), new TypeReference<List<User>>(){});
        //userList=objectMapper.readValue(users,List.class); -> error at runtime why? -> check onenote
    }
    public User getUser(){
        return user;
    }
    public Boolean userLogin(){
        Optional<User> userFound=userList.stream().filter(User1 -> User1.getName().equals(user.getName()) &&
                UserServiceUtil.checkPassword(user.getPassword(),User1.getHashedPassword())).findFirst();
        if(userFound.isPresent()){
            this.user=userFound.get();
            return true;
        }
        return false;
    }
    public Boolean signUp(User user1){
        try{
            userList.add(user1);
            saveUserListToFile();
            return Boolean.TRUE;
        }
        catch(Exception e){
            return Boolean.FALSE;
        }
    }
    private void saveUserListToFile() throws IOException{
        objectMapper.writeValue(new File(USERS_PATH),userList);
    }

    public void fetchBooking(){
        Optional<User> userFound=userList.stream().filter(User1 -> User1.getName().equals(user.getName()) &&
                UserServiceUtil.checkPassword(user.getPassword(),User1.getHashedPassword())).findFirst();
        if(userFound.isPresent()){
            userFound.get().printTickets();
        }
    }

    public Boolean cancelBooking(String ticketId){

        if(ticketId==null || ticketId.isEmpty())return false;

        Optional<User> useropt=userList.stream().filter(u-> u.getName().equals(user.getName())).findFirst(); // mai userList se nikalunga wo user taaki jab mai uski ticket delete kru to wo userList (that acts as my main db rn) usse bhi delete  ho jaye
        if(useropt.isPresent()){
            User actualuser=useropt.get();
            boolean removed=actualuser.getTicketsBooked().removeIf(ticket -> ticket.getTicketId().equals(ticketId));
            if(removed){
                try{
                    saveUserListToFile();
                }
                catch (IOException e){
                    return false;
                }
            }
            return removed;
        }
        return false;
    }

    public List<Train> getTrains(String src, String dest){
        try{
            TrainService trainService=new TrainService();
            return trainService.searchTrains(src,dest);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    public List<List<Integer>> fetchSeats(Train train){
        return train.getSeats();
    }

    public Boolean bookTrainSeats(int row,int col, Train train){
        try{
            TrainService trainService=new TrainService();
            List<List<Integer>> seats=train.getSeats();
            if(row>=0 && row<seats.size() && col>=0 && col<seats.get(row).size()){
                if(seats.get(row).get(col)==0){
                    seats.get(row).set(col,1);
                    train.setSeats(seats);
                    trainService.addTrain(train);
                    Ticket newTicket=new Ticket(UUID.randomUUID().toString(),user.getUserId(),"source","destination",java.time.LocalDate.now().toString(),train);
                    user.getTicketsBooked().add(newTicket);

                    return true;
                }
                else{
                    return false;
                }
            }
            else{
                return false;

            }

        }
        catch(IOException e){
            return Boolean.FALSE;
        }
    }






}
