import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{

    // Keep track of all clients, allows communications to multiple clients
    // static because belongs to class not object of each class
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    // Used to establish connection between client and server 
    private Socket socket;
    // Used to read message sent from client
    private BufferedReader bufferedReader;
    // Used to write messages/data sent from client
    private BufferedWriter bufferedWriter;
    private String clientUsername;

    public ClientHandler(Socket socket){
        try{
            this.socket = socket;
            // Character stream wrapping byte stream to send chars
            // Wrapping with bufferedwriter to increase efficency
            // This stream sends data
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            // This stream reads data
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // Essentially a Scanner obj function call
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("Server " + clientUsername + " has entered the chat");
        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    @Override
    public void run(){
        String messageFromClient;
        // Ran on seperate thread so rest of code isnt blocked due to blocking operation.
        while(socket.isConnected()){
            try{
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);
            }catch(IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
                // takes us out of infinite loop
                break;
            }
        }
    }
    // Send message to everyone in group chat
    public void broadcastMessage(String messageToSend){
        for(ClientHandler clientHandler : clientHandlers){
            try{
                if(!clientHandler.clientUsername.equals(clientUsername)){
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }catch(IOException e){
                closeEverything(socket,bufferedReader,bufferedWriter);
            }
        }
    }
    public void removeClientHandler(){
        // removes current client handler, removes client from list so they nolonger recieve messages
        clientHandlers.remove(this);
        System.out.println("Server: " + clientUsername + " has left the chat!");
    }
    // Closes conncetion and streams
    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        removeClientHandler();
        // Only need to close outer wrapper as underlying are closed with it.
        // Closing socket closes sockets input/output Stream
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
}
