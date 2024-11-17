package pojo;

public class GitHubUser {
    private String name;
    private String description;
    private boolean privateRepo;

    // Constructor
    public GitHubUser(String name, String description, boolean privateRepo) {
        this.name = name;
        this.description = description;
        this.privateRepo = privateRepo;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPrivateRepo() {
        return privateRepo;
    }

    public void setPrivateRepo(boolean privateRepo) {
        this.privateRepo = privateRepo;
    }
}
