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
                const form = document.getElementById(emergency_stop_folder_button.dataset.formId);
                form.submit();
            },
            () => { },
        );
    });
});

document.addEventListener("DOMContentLoaded", function () {
    const emergency_stop_job_button = document.getElementById("emergency-stop-job-pipeline-button");
    if (!emergency_stop_job_button) return;

    emergency_stop_job_button.addEventListener("click", function (event) {
        event.preventDefault();

        dialog.confirm("Abort this pipeline?", {
            message: "This will stop all running builds of this pipeline immediately. Are you sure?",
            cancelText: dialog.translations.no,
            okText: dialog.translations.yes,
            type: "destructive"
        }).then(
            () => {
                const form = document.getElementById(emergency_stop_job_button.dataset.formId);
                form.submit();
            },
            () => { },
        );
    });
});
