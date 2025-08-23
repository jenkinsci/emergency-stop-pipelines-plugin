document.addEventListener("DOMContentLoaded", function() {
    const form = document.getElementById("emergency-stop-form");
    if (!form) return;

    form.addEventListener("submit", function(event) {
        event.preventDefault();

        dialog.confirm("Abort all pipelines?", {
            message: "This will stop all running jobs immediately. Are you sure?",
            cancelText: dialog.translations.no,
            okText: dialog.translations.yes
        }).then(
            () => form.submit(),
            () => {},
        );
    });
});