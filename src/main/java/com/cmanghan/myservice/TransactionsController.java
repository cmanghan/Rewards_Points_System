package com.cmanghan.myservice;


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.text.ParseException;
import java.util.*;


@RestController
public class TransactionsController {

    List<Transaction> transactionList = new ArrayList<Transaction>();

    @PostMapping("/addTransaction")
    public ResponseEntity<String> addTransaction (@RequestBody Transaction newTransaction) {
        List<Transaction> tempList = new ArrayList<Transaction>();
        for (Transaction t: transactionList){
            tempList.add(t);
        }

        //if the transaction is positive
        if (newTransaction.getPoints() > 0) {
            transactionList.add(newTransaction);
            //sorted by date
            Collections.sort(transactionList);
            return ResponseEntity.status(HttpStatus.OK).body("Added Transaction");
        }
        //else the added transaction is negative
        else {
            //check to ensure no payer's points go negative
            var payerTotalPoints = 0;
            for (Transaction t : transactionList) {
                if (t.getPayer().equals(newTransaction.getPayer())) {
                    payerTotalPoints += t.getPoints();
                }
            }
            //if the negative add would make the payer's points go negative
            if (payerTotalPoints < Math.abs(newTransaction.getPoints())) {
                return ResponseEntity.badRequest().body("Error - payer cannot have negative points");
            }
            //else the payer has enough points to allow negative transaction
            else {
                while ( newTransaction.getPoints() < 0) {
                    for (Transaction t : tempList) {
                        if (t.getPayer().equals(newTransaction.getPayer())) {
                            //if the negative add (absolute) is greater than a single transaction
                            if (Math.abs(newTransaction.getPoints()) >= t.getPoints()) {
                                newTransaction.setPoints(newTransaction.getPoints() + t.getPoints());
                               //delete the transaction from the list
                                transactionList.remove(t);
                            }
                            //else the (remaining) negative add can be covered by the transaction
                            else {
                                t.setPoints(t.getPoints() + newTransaction.getPoints());
                                newTransaction.setPoints(0);
                                return ResponseEntity.status(HttpStatus.OK).body("Added Negative Transaction");
                            }
                        }
                    }
                }
                return ResponseEntity.badRequest().body("Added Negative Transaction");
            }
        }
    }


    @GetMapping("/spend")
    public ResponseEntity<String> spendPoints (@RequestParam int spend) {
        List<Transaction> tempList = new ArrayList<Transaction>();
        for (Transaction t: transactionList){
            tempList.add(t);
        }
        int totalPoints = 0;
        for (Transaction t : transactionList) {
            totalPoints += t.getPoints();
        }
        //a user cannot be left with negative points - cannot spend more than they have
        if (spend > totalPoints) {
            return ResponseEntity.badRequest().body("Total points spent cannot exceed total points accumulated");
        }
        //spent points cannot be zero
        else if (spend == 0) {
            return ResponseEntity.badRequest().body("Please enter a valid entry. Must be greater than 0.");
        }
        else {
            HashMap <String, Integer> tempMap = new HashMap<>();
            while (spend > 0) {
                int temp = 0;
                for (Transaction t : tempList) {
                    //if statement checks if the transaction has enough points to cover spend, if not also deducts from the next transaction
                    if (t.getPoints() <= spend) {

                        spend -= t.getPoints();
                        //remove if zero
                        transactionList.remove(t);

                        //check if key already exists
                        if (tempMap.containsKey(t.getPayer())){
                            tempMap.replace(t.getPayer(), tempMap.get(t.getPayer()), tempMap.get(t.getPayer() + t.getPoints()));
                        }
                        //otherwise add key/value
                        else{
                            tempMap.put(t.getPayer(), t.getPoints());
                        }
                        t.setPoints(0);
                    } else {

                        t.setPoints(t.getPoints() - spend);

                        //check if key already exists
                        if (tempMap.containsKey(t.getPayer())){
                            tempMap.replace(t.getPayer(), tempMap.get(t.getPayer()), tempMap.get(t.getPayer() + spend));
                        }
                        //otherwise add key/value
                        else{
                            tempMap.put(t.getPayer(), spend);
                        }
                        //set spend to zero to stop loop
                        spend = 0;
                        return ResponseEntity.status(HttpStatus.OK).body("Spend Successful. Points paid out are as follows: " + tempMap);
                    }
                }
            }

        }
        return ResponseEntity.status(HttpStatus.OK).body("Spend Successful");
    }

    @GetMapping("/getList")
    public List getList() {
        return transactionList;
    }

}
