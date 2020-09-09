package co.astrnt.profile.data.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CategoryApiResponse extends BaseApiResponse {

    @Expose
    @SerializedName("list_questions")
    private List<CategoryResponse> categories;

    public List<CategoryResponse> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryResponse> categories) {
        this.categories = categories;
    }
}
