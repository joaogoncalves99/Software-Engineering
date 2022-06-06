<template>
  <v-container v-if="difficultQuestions != null" fluid>
    <v-card class="table">
      <v-data-table
        :headers="headers"
        :items="difficultQuestions"
        :sort-by="['percentage']"
        sort-desc
        :mobile-breakpoint="0"
        :items-per-page="15"
        :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
      >
        <template v-slot:top>
          <v-card-title>
            <span class="text-subtitle-1">Difficult Questions</span>
            <v-spacer></v-spacer>
            <v-btn
              color="primary"
              dark
              data-cy="updateDifficultQuestionsButton"
              @click="onRefresh"
              >REFRESH</v-btn
            >
          </v-card-title>
        </template>
        <template v-slot:[`item.answered`]="{ item }">
          <div v-if="item.answered === true">Yes</div>
          <div v-else>No</div>
        </template>
        <template v-slot:[`item.action`]="{ item }">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                v-on="on"
                data-cy="showStudentViewDialogButton"
                @click="showStudentViewDialog(item.questionDto)"
                color="grey"
                >school</v-icon
              >
            </template>
            <span>Student View</span>
          </v-tooltip>
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                class="mr-2 action-button"
                v-on="on"
                data-cy="deleteDifficultQuestionButton"
                @click="deleteDifficultQuestion(item)"
                color="red"
                >delete
              </v-icon>
            </template>
            <span>Delete Question</span>
          </v-tooltip>
        </template>
      </v-data-table>
      <student-view-dialog
        v-if="statementQuestion && studentViewDialog"
        v-model="studentViewDialog"
        :statementQuestion="statementQuestion"
        v-on:close-show-question-dialog="onCloseStudentViewDialog"
      />
    </v-card>
  </v-container>
</template>

<script lang="ts">
import { Component, Prop, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import Question from '@/models/management/Question';
import DifficultQuestion from '@/models/dashboard/DifficultQuestion';
import StatementQuestion from '@/models/statement/StatementQuestion';
import StudentViewDialog from '@/views/teacher/questions/StudentViewDialog.vue';
@Component({
  components: {
    'student-view-dialog': StudentViewDialog,
  },
})
export default class DifficultQuestionsView extends Vue {
  @Prop() dashboardId!: number;
  @Prop() lastCheckDifficultQuestions!: string;
  difficultQuestions: DifficultQuestion[] = [];
  studentViewDialog: boolean = false;
  statementQuestion: StatementQuestion | null = null;
  async created() {
    await this.$store.dispatch('loading');
    try {
      this.difficultQuestions = await RemoteServices.getDifficultQuestions(
        this.dashboardId
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }
  async onRefresh() {
    try {
      await RemoteServices.updateDifficultQuestions(this.dashboardId);
      this.difficultQuestions = await RemoteServices.getDifficultQuestions(
        this.dashboardId
      );
      let dashboard = await RemoteServices.getUserDashboard();
      this.$emit('refresh', dashboard.lastCheckDifficultQuestions);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
  async deleteDifficultQuestion(difficultQuestion: DifficultQuestion) {
    try {
      await RemoteServices.deleteDifficultQuestion(difficultQuestion.id);
      this.difficultQuestions = this.difficultQuestions.filter(
        (difficultQuestion1) => difficultQuestion1.id != difficultQuestion.id
      );
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
  async showStudentViewDialog(question: Question) {
    if (question.id) {
      try {
        this.statementQuestion = await RemoteServices.getStatementQuestion(
          question.id
        );
        this.studentViewDialog = true;
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }
  onCloseStudentViewDialog() {
    this.statementQuestion = null;
    this.studentViewDialog = false;
  }
  headers: object = [
    {
      text: 'Actions',
      value: 'action',
      align: 'left',
      width: '5px',
      sortable: false,
    },
    {
      text: 'Question',
      value: 'questionDto.content',
      width: '5px',
      align: 'center',
    },
    {
      text: 'Percentage',
      value: 'percentage',
      width: '5px',
      align: 'right',
    },
  ];
}
</script>

<style lang="scss" scoped></style>
