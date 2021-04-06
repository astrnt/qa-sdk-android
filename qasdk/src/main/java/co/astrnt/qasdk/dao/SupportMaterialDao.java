package co.astrnt.qasdk.dao;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SupportMaterialDao extends RealmObject {

	@PrimaryKey
	@SerializedName("id")
	private int id;
	@Expose
	@SerializedName("size")
	private int size;
	@Expose
	@SerializedName("name")
	private String name;
	@Expose
	@SerializedName("title")
	private String title;
	@Expose
	@SerializedName("type")
	private String type;
	@Expose
	@SerializedName("url")
	private String url;
	@Expose
	@SerializedName("offline_path")
	private String offlinePath;

	public String getOfflinePath() {
		return offlinePath;
	}

	public void setOfflinePath(String offlinePath) {
		this.offlinePath = offlinePath;
	}

	public void setSize(int size){
		this.size = size;
	}

	public int getSize(){
		return size;
	}

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setTitle(String title){
		this.title = title;
	}

	public String getTitle(){
		return title;
	}

	public void setType(String type){
		this.type = type;
	}

	public String getType(){
		return type;
	}

	public void setUrl(String url){
		this.url = url;
	}

	public String getUrl(){
		return url;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}