import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client{
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String username;
    
    public Client(Socket socket, String username){
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;
        }catch(IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }
    public void sendMessage(){
        try{
            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scnr = new Scanner(System.in);
            while(socket.isConnected()){
                String messageToSend = scnr.nextLine();
                bufferedWriter.write(username + ": "+ messageToSend);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            scnr.close();
            
        }catch(IOException e){
            closeEverything(socket,bufferedReader, bufferedWriter);

        }
    }

    public void listenForMessage(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                String msgFromGroupChat;

                while(socket.isConnected()){
                    try{
                        msgFromGroupChat = bufferedReader.readLine();
                        System.out.println(msgFromGroupChat);
                    }catch(IOException e ){
                        closeEverything(socket,bufferedReader, bufferedWriter);
                    }
                }
            }  
        }).start();
    }
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket !=null){
                socket.close();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
        public static void main(String[] args) throws UnknownHostException, IOException{
            Scanner scanner = new Scanner(System.in);
            System.out.println("Welcome to the Server! Enter your username for the group chat: ");
            String username = scanner.nextLine();
            try (Socket socket = new Socket("localhost", 1234)) {
                Client client = new Client(socket, username);
                client.listenForMessage();
                client.sendMessage();
                scanner.close();
            }


        }
    }
