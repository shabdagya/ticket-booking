package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.entities.Train;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TrainService {

    private List<Train> trainList;
    private ObjectMapper objectMapper =new ObjectMapper();
    private static final String TRAIN_DB_PATH="../localdb/trains.json";

    public TrainService() throws IOException{
        loadTrainFromFile();

    }
    private void loadTrainFromFile() throws IOException{
        trainList = objectMapper.readValue(new File(TRAIN_DB_PATH), new TypeReference<List<Train>>() {});
    }
    public List<Train> searchTrains(String src,String dest){
        return trainList.stream().filter(train-> validTrain(train,src,dest)).collect(Collectors.toList());
    }
    public boolean validTrain(Train train,String src,String dest ){
        List<String> stationOrder=train.getStations();

        int srcidx=stationOrder.indexOf(src.toLowerCase());
        int destidx=stationOrder.indexOf(dest.toLowerCase());

        return srcidx!=-1 && destidx!=-1 && srcidx<destidx;
    }
    public void addTrain(Train newtrain){
        Optional<Train> existingTrain=trainList.stream().filter(train -> train.getTrainId().equals(newtrain.getTrainId())).findFirst();
        if(existingTrain.isPresent()){
            updateTrain(newtrain);
        }
        else{
            trainList.add(newtrain);
            saveTrainListToFile();

        }
    }
    public void saveTrainListToFile(){
        try{
            objectMapper.writeValue(new File(TRAIN_DB_PATH),trainList);
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    public void updateTrain(Train newTrain){
        OptionalInt index= IntStream.range(0,trainList.size()).filter(i->trainList.get(i).getTrainId().equals(newTrain.getTrainId())).findFirst();
            if(index.isPresent()){
            trainList.set(index.getAsInt(),newTrain);
            saveTrainListToFile();
        }
        else{
            trainList.add(newTrain);
            saveTrainListToFile();
        }


    }

}
