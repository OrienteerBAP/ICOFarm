
function addAutoCopyOnElement(inputId, buttonId) {
    $('#' + buttonId).click(function() {
        $('#' + inputId).select();
        document.execCommand("Copy");
    });
}