document.addEventListener("DOMContentLoaded", function() {
    const form = document.getElementById("emergency-stop-form");
    if (!form) return;

    form.addEventListener("submit", function(event) {
    event.preventDefault(); // wait for user confirmation

    dialog.confirm("Abort all pipelines?", {
        message: "This will stop all running jobs immediately. Are you sure?",
        cancelText: dialog.translations.no,
        okText: dialog.translations.yes
    }).then(
        () => {
        // User confirmed
        console.log("Pipelines aborted!")
        form.submit();
        },
        () => {
        // User canceled
        }
    );
    });
});