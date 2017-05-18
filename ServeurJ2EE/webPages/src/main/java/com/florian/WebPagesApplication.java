package com.florian;

import com.oracle.javafx.jmx.json.JSONException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

@SpringBootApplication
public class WebPagesApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebPagesApplication.class, args);
	}
}


@Controller
class ReservationMvcController{
	private String token;
	@RequestMapping("/login")
	String reservation(Model model) {
		model.addAttribute("test",new Client());
		model.addAttribute("carac",new carac());
		return "login"; // src/main/ressources/template/+$x+.html
	}


	@RequestMapping("/list")
	String list(Model model) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httppost = new HttpGet("http://localhost:9999/reservations/names");
		httppost.setHeader("Authorization", "bearer " +  token);
		httppost.addHeader("accept", "application/json");

		try {



			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			String retourString  =EntityUtils.toString(response.getEntity());
			System.out.println(retourString);
			JSONObject retour= null;
			retour = new JSONObject(retourString);
			LinkedList<carac> lis= new LinkedList<carac>() ;
			JSONArray personne = retour.getJSONArray("reservatiopn");
			for (int i = 0; i < personne.length(); i++) {
				JSONObject getJSonObj = (JSONObject) personne.get(i);
				String personneNOm = getJSonObj.getString("nom");
				String prenom = getJSonObj.getString("prenom");

				String idUser = getJSonObj.getString("idUser");
				String symptome = getJSonObj.getString("symptome");
				String note = getJSonObj.getString("note");
				String genre = getJSonObj.getString("genre");
//				String noteDate = getJSonObj.getString("noteDate");
				carac caract=new carac( personneNOm,prenom,genre,idUser,symptome,note);
				lis.add(caract);
			}
			System.out.println(lis);
			model.addAttribute("listPatient",lis);
			model.addAttribute("carac",new carac());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return  "list";
	}


	@RequestMapping("/logout")
	String logout(Model model,HttpSession session ) {
		session.removeAttribute("token");
		session.removeAttribute("name");
		model.addAttribute("test",new Client());
		model.addAttribute("carac",new carac());
		return "login"; // src/main/ressources/template/+$x+.html
	}
	@RequestMapping("/update")
	String update(Model model,HttpSession session ) {
		session.removeAttribute("token");
		session.removeAttribute("name");
		model.addAttribute("test",new Client());
		model.addAttribute("carac",new carac());
		return "update"; // src/main/ressources/template/+$x+.html
	}
	@RequestMapping("/mainPage")
	String mainPage(Model model,HttpSession session ) {
		session.removeAttribute("token");
		session.removeAttribute("name");
		model.addAttribute("test",new Client());
		model.addAttribute("carac",new carac());
		return "mainPage"; // src/main/ressources/template/+$x+.html
	}


	@RequestMapping(value = "/tryAuthentification", method = RequestMethod.POST)
	public String greetingSubmit(@ModelAttribute("test") Client client, HttpSession session, Model model, @Value("${client_id}") String clientId,
								 @Value("${client_secret}") String client_secret, @Value("${grant_type}") String grantType, @Value("${url_oAuthServeur}")String url) {


		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		httppost.setHeader("Authorization", "Basic " +  Base64.getEncoder().encodeToString((clientId+":"+client_secret).getBytes()));
		httppost.addHeader("accept", "application/json");

		try {
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("grant_type",grantType));
			nameValuePairs.add(new BasicNameValuePair("username", client.getName()));
			nameValuePairs.add(new BasicNameValuePair("password", client.getPassword()));
			nameValuePairs.add(new BasicNameValuePair("client_id", clientId));
			nameValuePairs.add(new BasicNameValuePair("client_secret", client_secret));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);

			JSONObject json_auth = new JSONObject(EntityUtils.toString(response.getEntity()));
			if(json_auth.has("access_token")) {
				String token = json_auth.getString("access_token");
				session.setAttribute("token",token);
				session.setAttribute("name",client.getName());
				model.addAttribute("carac",new carac());
				this.token=token;
				System.out.println(token);
				return  "mainPage";
			}else if( json_auth.has("error")){
				System.out.println("erreur authenification");
				model.addAttribute("loged",false);
				model.addAttribute("carac",new carac());
				return "login";
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}


		//TODO Create a page errorPage
		return "errorPage";


	}





	@RequestMapping(value = "/updateNom", method = RequestMethod.POST)
	public String updateNOm(@ModelAttribute("test") carac caract, HttpSession session, Model model, @Value("${client_id}") String clientId,
							@Value("${client_secret}") String client_secret, @Value("${grant_type}") String grantType, @Value("${url_oAuthServeur}")String url) {

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://localhost:8000/update");
		try {

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("nom",caract.getNom()));
			nameValuePairs.add(new BasicNameValuePair("note", caract.getNote()));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			System.out.println(EntityUtils.toString(response.getEntity()));
			session.setAttribute("token",token);
			model.addAttribute("carac",new carac());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}




		return  "mainPage";
	}






	@RequestMapping(value = "/sendcaract", method = RequestMethod.POST)
	public String sendcarac(@ModelAttribute("test") carac caract, HttpSession session, Model model, @Value("${client_id}") String clientId,
								 @Value("${client_secret}") String client_secret, @Value("${grant_type}") String grantType, @Value("${url_oAuthServeur}")String url) {

		System.out.println("sendCaract + "+ token);
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://localhost:9999/reservations/test");
		httppost.setHeader("Authorization", "bearer " +  token);
		httppost.addHeader("accept", "application/json");

		try {

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("nom",caract.getNom()));
			nameValuePairs.add(new BasicNameValuePair("prenom", caract.getPrenom()));
			nameValuePairs.add(new BasicNameValuePair("genre", caract.getGenre()));
			nameValuePairs.add(new BasicNameValuePair("idUser", caract.getIdUser()));
			nameValuePairs.add(new BasicNameValuePair("symptome", caract.getSymptome()));
			nameValuePairs.add(new BasicNameValuePair("note", caract.getNote()));
			nameValuePairs.add(new BasicNameValuePair("noteDate", caract.getNoteDate()));

			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			System.out.println(EntityUtils.toString(response.getEntity()));
			session.setAttribute("token",token);
			model.addAttribute("carac",new carac());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}




		return  "mainPage";
	}



}

class carac{
		private String nom;
	private String prenom;
	private String genre;
	private String idUser;
	private String symptome;
	private String note;
	private String noteDate;

	public carac(String nom, String prenom, String genre, String idUser, String symptome, String note, String noteDate) {
		this.nom = nom;
		this.prenom = prenom;
		this.genre = genre;
		this.idUser = idUser;
		this.symptome = symptome;
		this.note = note;
		this.noteDate = noteDate;
	}

	public carac() {
	}

	public carac(String nom, String prenom, String genre, String idUser, String symptome, String note) {
		this.nom = nom;
		this.prenom = prenom;
		this.genre = genre;
		this.idUser = idUser;
		this.symptome = symptome;
		this.note = note;
	}

	@Override
	public String toString() {
		return "carac{" +
				"nom='" + nom + '\'' +
				", prenom='" + prenom + '\'' +
				", genre='" + genre + '\'' +
				", idUser='" + idUser + '\'' +
				", symptome='" + symptome + '\'' +
				", note='" + note + '\'' +
				", noteDate='" + noteDate + '\'' +
				'}';
	}

	public String getNoteDate() {
		return noteDate;
	}

	public void setNoteDate(String noteDate) {
		this.noteDate = noteDate;
	}

	public String getNom() {
		return nom;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getIdUser() {
		return idUser;
	}

	public void setIdUser(String idUser) {
		this.idUser = idUser;
	}

	public String getSymptome() {
		return symptome;
	}

	public void setSymptome(String symptome) {
		this.symptome = symptome;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public void setNom(String nom) {
		this.nom = nom;
	}

	public String getPrenom() {
		return prenom;
	}

	public void setPrenom(String prenom) {
		this.prenom = prenom;
	}
}