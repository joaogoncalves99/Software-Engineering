describe('Student Walkthrough', () => {
    beforeEach(() => {
        //create quiz
        cy.demoTeacherLogin();
        cy.createQuestion(
            'Question DifficultQuestion 1',
            'Question',
            'Option',
            'Option',
            'ChooseThisWrong',
            'Correct'
        );
        cy.createQuestion(
            'Question DifficultQuestion 2',
            'Question',
            'Option',
            'Option',
            'ChooseThisWrong',
            'Correct'
        );
        cy.createQuizzWith2Questions(
            'DifficultQuestion Title',
            'Question DifficultQuestion 1',
            'Question DifficultQuestion 2'
        );
        cy.contains('Logout').click();
    });

    afterEach(() => {
        cy.deleteDifficultQuestion();
        cy.deleteQuestionsAndAnswers();

    });


    it('student creates discussion', () => {
        cy.intercept('GET', '**/students/dashboards/executions/*').as('getDashboard');
        cy.intercept('GET', '**/students/dashboard/difficultquestion/*').as('getDifficultQuestions');
        cy.intercept('PUT', '**/students/dashboard/difficultquestion/update/*').as('updateDifficultQuestions');

        cy.demoStudentLogin();

        cy.solveQuizzDifficultQuestion('DifficultQuestion Title',2);

        cy.get('[data-cy="dashboardMenuButton"]').click();
        cy.wait('@getDashboard');

        cy.get('[data-cy="difficultQuestionsMenuButton"]').click();
        cy.wait('@getDifficultQuestions');

        cy.get('[data-cy="updateDifficultQuestionsButton"]').click();
        cy.wait('@getDifficultQuestions');

        cy.get(':nth-child(1) > .text-left > [data-cy="showStudentViewDialogButton"]').click();
        cy.get('.v-card__actions > .v-btn > .v-btn__content').click();

        cy.intercept('DELETE', '**/students/dashboard/difficultquestion/remove/*').as('deleteDifficultQuestion');

        cy.get(':nth-child(1) > .text-left > [data-cy="deleteDifficultQuestionButton"]').click();
        cy.wait('@deleteDifficultQuestion');

        cy.get('[data-cy="deleteDifficultQuestionButton"]').click();
        cy.wait('@deleteDifficultQuestion');



        cy.contains('Logout').click();
        Cypress.on('uncaught:exception', (err, runnable) => {
            // returning false here prevents Cypress from
            // failing the test
            return false;
        });
    });
});