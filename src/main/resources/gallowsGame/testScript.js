getParam = (param) => {
    const queryString = window.location.search;
    const urlParams = new URLSearchParams(queryString);
    return urlParams.get(param);
}


async function getWordLength() {
    let name = getParam("name");
    let responce = await fetch(`http://localhost:8080/gallowsGame/getWordLength?name=${name}&category=oop`);
    s.textContent = await responce.text();
}


async function checkChar(character) {
    let name = getParam("name");
    let responce = await fetch(`http://localhost:8080/gallowsGame/checkChar?name=${name}&character=${character}`);
    s.textContent = await responce.text();
}

let b = document.getElementById("random-number-button");
let s = document.getElementById("random-number-show");

b.addEventListener("click", e => getWordLength());

document.addEventListener("keydown", e => {
    checkChar(e.key);
})

