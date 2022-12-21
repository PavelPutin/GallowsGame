const categorySelect = document.getElementById("category-select");
(async () => {
    let categories = await fetch("http://localhost:8080/gallowsGame/getCategories");
    categories = await categories.text();
    categories = categories.split(",");
    for (const category of categories) {
        let option = document.createElement("option");
        option.setAttribute("value", category);
        option.textContent = category;
        categorySelect.append(option);
    }
})();

async function startGame(category) {
    let queryString = window.location.search;
    let params = new URLSearchParams(queryString);

    let username = encodeURIComponent(params.get("username"));
    let passwordHash = encodeURIComponent(params.get("passwordHash"));
    let encodedCategory = encodeURIComponent(category);
    let gameId = await fetch(`http://localhost:8080/gallowsGame/startGame?username=${username}&passwordHash=${passwordHash}&category=${encodedCategory}`);
    gameId = await gameId.text();

    let gameURL = `http://localhost:8080/gallowsGame/game?username=${username}&passwordHash=${passwordHash}&gameId=${gameId}`;
    console.log(gameURL);
    window.location.replace(gameURL);
}

const startGameButton = document.getElementById("start-game-button");
startGameButton.addEventListener("click", async (e) => {
    let category = categorySelect.value;
    console.log(category);
    await startGame(category);
})