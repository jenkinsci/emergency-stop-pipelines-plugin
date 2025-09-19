document.addEventListener("DOMContentLoaded", function () {
    const form = document.getElementById("emergency-stop-form");
    if (!form) return;

    form.addEventListener("submit", function (event) {
        event.preventDefault();

        dialog.confirm("Abort all pipelines?", {
            message: "This will stop all running jobs immediately. Are you sure?",
            cancelText: dialog.translations.no,
            okText: dialog.translations.yes,
            type: "destructive"
        }).then(
            () => form.submit(),
            () => { },
        );
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const emergency_stop_folder_button = document.getElementById("emergency-stop-folder-pipeline-button");
    if (!emergency_stop_folder_button) return;

    emergency_stop_folder_button.addEventListener("click", function (event) {
        event.preventDefault();

        dialog.confirm("Abort all pipelines?", {
            message: "This will stop all running jobs immediately. Are you sure?",
            cancelText: dialog.translations.no,
            okText: dialog.translations.yes,
            type: "destructive"
        }).then(
            () => {
                fetch(emergency_stop_folder_button.dataset.submitUrl, {
                    method: "GET",
                    credentials: "same-origin"
                }).then(() => {
                    notificationBar.show('Triggered emergency stop of folder pipelines.', notificationBar.WARNING)
                }).catch(err => {
                    notificationBar.show('Failed to stop pipelines.', notificationBar.ERROR)
                    alert("Failed to stop pipelines: " + err);
                });
            },
            () => { },
        );
    });
});