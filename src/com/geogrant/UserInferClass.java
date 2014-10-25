package com.geogrant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import cc.mallet.classify.Classifier;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.Labeling;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

import java.util.Iterator;
import java.util.List;
/**
 * Servlet implementation class UserInferClass
 */
@WebServlet("/UserInferClass")
public class UserInferClass extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UserInferClass() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
		if (request.getParameterMap().containsKey("username")) {
			String user = request.getParameter("username");
			int pagenum = 1;
			int countnum = 100;
			if (request.getParameterMap().containsKey("page"))
				pagenum = Integer.parseInt(request.getParameter("page"));
			if (request.getParameterMap().containsKey("count"))
				countnum = Integer.parseInt(request.getParameter("count"));
			try {
				Classifier classifier = loadClassifier();
				getUserTimeline(user, classifier, response, pagenum, countnum);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} else {
			out.print("Please provide a valid twitter username (username parameter).");
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	public static void getUserTimeline(String user, Classifier classifier, HttpServletResponse response, int pagenum, int countnum) throws IOException {
        // gets Twitter instance with default credentials
		response.setContentType("application/json");
		PrintWriter out = response.getWriter();
        Twitter twitter = new TwitterFactory().getInstance();
        try {
            List<Status> statuses;
            Paging page = new Paging(pagenum, countnum);
            statuses = twitter.getUserTimeline(user, page);
            String responseObject = "{\"user\":\""+user+"\",\"tweets\":[";
            for (Status status : statuses) {
                // System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
            	
                if (status.getText().length() >= 50) {
                	String tweet = status.getText();
            		try {
            			tweet = "1 personal "+tweet;
            			responseObject += "{\""+status.getId()+"\": {\"datetime\":\"" + status.getCreatedAt().toString() + "\",";
            			responseObject += "\"classification\":"+printLabelings(classifier, tweet, out) + "}},";
            		} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
    				
    		    } else {
    				// out.print("Please enter at least 50 characters for the tweet content.");
    		    	// TWEET CONTENT NOT LONG ENOUGH
    			}
            }
            responseObject = responseObject.substring(0, responseObject.length()-1);
            out.print(responseObject + "]}");
        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to get timeline: " + te.getMessage());
            System.exit(-1);
        }
    }
	
	public Classifier loadClassifier() throws FileNotFoundException, IOException, ClassNotFoundException {

        // The standard way to save classifiers and Mallet data                                            
        //  for repeated use is through Java serialization.                                                
        // Here we load a serialized classifier from a file.                                               

        Classifier classifier;

        File file = new File(getServletContext().getRealPath("/WEB-INF/tweets.classifier"));
        ObjectInputStream ois = new ObjectInputStream (new FileInputStream (file));
        classifier = (Classifier) ois.readObject();
        ois.close();

        return classifier;
    }
	
	public static String printLabelings(Classifier classifier, String tweet, PrintWriter out) throws IOException {

		
		
        // Create a new iterator that will read raw instance data from                                     
        //  the lines of a file.                                                                           
        // Lines should be formatted as:                                                                   
        //                                                                                                 
        //   [name] [label] [data ... ]                                                                    
        //                                                                                                 
        //  in this case, "label" is ignored.                                                              

        CsvIterator reader = new CsvIterator(new StringReader(tweet), "(\\w+)\\s+(\\w+)\\s+(.*)", 3, 2, 1);  // (data, label, name) field indices               

        // Create an iterator that will pass each instance through                                         
        //  the same pipe that was used to create the training data                                        
        //  for the classifier.                                                                            
        Iterator instances = classifier.getInstancePipe().newIteratorFrom(reader);

        // Classifier.classify() returns a Classification object                                           
        //  that includes the instance, the classifier, and the                                            
        //  classification results (the labeling). Here we only                                            
        //  care about the Labeling.  
        String responseObj = "{";
        while (instances.hasNext()) {
            Labeling labeling = classifier.classify(instances.next()).getLabeling();
            for (int rank = 0; rank < labeling.numLocations(); rank++){
                responseObj += "\""+labeling.getLabelAtRank(rank) + "\":" + labeling.getValueAtRank(rank);
                if (rank < labeling.numLocations()-1)
                	responseObj += ", ";
            }
            // System.out.println();
        }
    	responseObj += "}";
    	return responseObj;
    }

}
